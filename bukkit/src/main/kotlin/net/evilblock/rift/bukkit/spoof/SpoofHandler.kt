package net.evilblock.rift.bukkit.spoof

import com.google.gson.JsonArray
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.util.Chance
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.permissions.user.UserHandler
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.rift.Rift
import net.evilblock.rift.bukkit.RiftBukkitPlugin
import net.evilblock.rift.bukkit.spoof.listener.SpoofListeners
import net.evilblock.rift.bukkit.spoof.store.RedisBungeeSpoof
import net.evilblock.rift.bukkit.spoof.thread.SpoofThread
import net.evilblock.rift.bukkit.spoof.v1_12_R1.FakeEntityPlayer
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

object SpoofHandler {

    private var enabled: Boolean = false
    private var paused: Boolean = false

    private var profiles: JsonArray = JsonArray()
    private var fakePlayers: MutableMap<UUID, FakeEntityPlayer> = ConcurrentHashMap()

    private var tasks: MutableList<BukkitTask> = arrayListOf()

    fun initialLoad() {
        enabled = RiftBukkitPlugin.instance.readSpoofEnabled()
        paused = RiftBukkitPlugin.instance.readSpoofPaused()

        if (enabled) {
            profiles = loadProfiles()
        }

        Cubed.instance.redis.runRedisCommand { redis ->
            RedisBungeeSpoof.cleanupLaggedPlayers(redis)
        }

        SpoofThread.start()

        Tasks.asyncTimer(0, 20L * 3) {
            Cubed.instance.redis.runRedisCommand { redis ->
                redis.hset("heartbeats", getSpoofedProxyId(), System.currentTimeMillis().toString())
            }
        }

        Tasks.asyncTimer(0, 20L * 5) {
            if (isEnabled() && !isPaused()) {
                syncPings()
            }
        }

        Bukkit.getServer().pluginManager.registerEvents(SpoofListeners, RiftBukkitPlugin.instance)
    }

    fun onDisable() {
        for (fakePlayer in fakePlayers.values) {
            removeFakePlayer(fakePlayer, false)
        }
    }

    fun getSpoofedProxyId(): String {
        return RiftBukkitPlugin.instance.readProxyId() + "-spoof"
    }

    fun isEnabled(): Boolean {
        return enabled
    }

    fun toggle() {
        enabled = !enabled

        if (enabled) {
            profiles = loadProfiles()
        } else {
            profiles = JsonArray()

            for (task in tasks) {
                if (!task.isCancelled) {
                    task.cancel()
                }
            }
        }
    }

    fun isPaused(): Boolean {
        return paused
    }

    fun togglePause() {
        paused = !paused
    }

    private fun loadProfiles(): JsonArray {
        return Rift.GSON.fromJson(FileReader(File(Rift.instance.plugin.getDirectory(), "profiles.json")), JsonArray::class.java)
    }

    fun getRealPlayerCount(): Int {
        return Bukkit.getOnlinePlayers().size - fakePlayers.size
    }

    fun getFakePlayers(): Map<UUID, FakeEntityPlayer> {
        return fakePlayers
    }

    fun isFakePlayer(player: Player): Boolean {
        return fakePlayers.contains(player.uniqueId)
    }

    fun addFakePlayer(player: FakeEntityPlayer) {
        if (RiftBukkitPlugin.instance.server.hasWhitelist()) {
            throw IllegalStateException("Cannot add fake player while the server is whitelisted!")
        }

        val existing = Bukkit.getPlayer(player.name)
        if (existing != null) {
            removeFakePlayer((existing as CraftPlayer).handle as FakeEntityPlayer)
            return
        }

        player.ping = ThreadLocalRandom.current().nextInt(10, 200)

        val bukkitPlayer = player.bukkitEntity
        bukkitPlayer.displayName = player.name

        // add to map THEN spawn fake player
        fakePlayers[player.uniqueID] = player

        Cubed.instance.redis.runRedisCommand { redis ->
            RedisBungeeSpoof.cachePlayer(bukkitPlayer, redis)
            RedisBungeeSpoof.addPlayer(bukkitPlayer, redis)
        }

        val permsUser = UserHandler.loadOrCreate(player.uniqueID)
        if (permsUser.grants.isEmpty()) {
            val ranks = RiftBukkitPlugin.instance.readSpoofRanks()
            if (ranks.isNotEmpty()) {
                val grant = Grant()
                grant.rank = Chance.weightedPick(ranks.keys.toList()) { ranks.getOrDefault(it, 0.0) }
                grant.reason = "Buycraft (GS)"

                permsUser.grants.add(grant)
                permsUser.requiresSave = true
            }
        }

        UserHandler.cacheUser(permsUser)

        Tasks.sync {
            MinecraftServer.getServer().playerList.onPlayerJoin(player, null)

            Tasks.delayed(1L) {
                val readCommands = RiftBukkitPlugin.instance.readSpoofActions()

                val toPerform = arrayListOf<String>()
                toPerform.addAll(readCommands.filter { it.second >= 100.0 }.map { it.first })
                toPerform.add(Chance.weightedPick(readCommands) { it.second }.first)

                for (command in toPerform) {
                    player.bukkitEntity.performCommand(command)
                }
            }
        }
    }

    fun removeFakePlayer(player: FakeEntityPlayer, forceAsync: Boolean = true) {
        // de-spawn fake player THEN remove from map
        MinecraftServer.getServer().playerList.disconnect(player)
        fakePlayers.remove(player.uniqueID)

        if (forceAsync) {
            Tasks.async {
                Cubed.instance.redis.runRedisCommand { redis ->
                    RedisBungeeSpoof.removePlayer(player.bukkitEntity, redis)
                }
            }
        } else {
            Cubed.instance.redis.runRedisCommand { redis ->
                RedisBungeeSpoof.removePlayer(player.bukkitEntity, redis)
            }
        }
    }

    fun nextRandomProfile(): JsonArray? {
        return if (profiles.size() == 0) {
            null
        } else {
            profiles[ThreadLocalRandom.current().nextInt(profiles.size())].asJsonArray
        }
    }

    private fun syncPings() {
        for (player in RiftBukkitPlugin.instance.server.onlinePlayers) {
            val nmsPlayer = (player as CraftPlayer).handle
            if (nmsPlayer is FakeEntityPlayer) {
                nmsPlayer.ping = max(min(ThreadLocalRandom.current().nextInt(nmsPlayer.ping - 5, nmsPlayer.ping + 5), 200), 20)
            }
        }
    }

}