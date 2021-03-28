package com.minexd.rift.bukkit.spoof

import com.google.gson.JsonArray
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.util.math.Chance
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.nms.MinecraftReflection
import com.minexd.rift.Rift
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.event.SpoofPlayerAddEvent
import com.minexd.rift.bukkit.spoof.event.SpoofPlayerRemoveEvent
import com.minexd.rift.bukkit.spoof.store.RedisBungeeSpoof
import com.minexd.rift.bukkit.spoof.thread.SpoofThread
import com.minexd.rift.bukkit.spoof.v1_8_R3.FakeEntityPlayer
import net.evilblock.cubed.serializers.Serializers
import net.minecraft.server.v1_8_R3.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
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

    var DEBUG: Boolean = false

    private var enabled: Boolean = false
    private var paused: Boolean = false

    private var profiles: JsonArray = JsonArray()
    private var fakePlayers: ConcurrentHashMap<UUID, FakeEntityPlayer> = ConcurrentHashMap()

    private var tasks: MutableList<BukkitTask> = arrayListOf()

    fun initialLoad() {
        enabled = RiftBukkitPlugin.instance.readSpoofEnabled()
        paused = RiftBukkitPlugin.instance.readSpoofPaused()

        if (enabled) {
            profiles =
                loadProfiles()
        }

        Cubed.instance.redis.runRedisCommand { redis ->
            RedisBungeeSpoof.cleanupLaggedPlayers(redis)
        }

        SpoofThread.start()

        Tasks.asyncTimer(0, 20L * 3) {
            Cubed.instance.redis.runRedisCommand { redis ->
                redis.hset("heartbeats",
                    getSpoofedProxyId(), System.currentTimeMillis().toString())
            }
        }

        Tasks.asyncTimer(0, 20L * 5) {
            if (isEnabled() && !isPaused()) {
                updatePings()
            }
        }
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
            profiles =
                loadProfiles()
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
        return Serializers.gson.fromJson(FileReader(File(Rift.instance.plugin.getDirectory(), "profiles.json")), JsonArray::class.java)
    }

    fun getRealPlayerCount(): Int {
        return Bukkit.getOnlinePlayers().size - fakePlayers.size
    }

    fun getFakePlayerCount(): Int {
        return Bukkit.getOnlinePlayers().count { MinecraftReflection.getHandle(it)::class.java.simpleName == "FakeEntityPlayer" }
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
            if (existing is CraftPlayer) {
                removeFakePlayer(existing.handle as FakeEntityPlayer)
            }
            return
        }

        if (!SpoofPlayerAddEvent(player).call()) {
            return
        }

        player.ping = ThreadLocalRandom.current().nextInt(10, 130)

        val bukkitPlayer = player.bukkitEntity
        bukkitPlayer.displayName = player.name

        // add to map THEN spawn fake player
        fakePlayers[player.uniqueID] = player

        Cubed.instance.redis.runRedisCommand { redis ->
            RedisBungeeSpoof.cachePlayer(bukkitPlayer, redis)
            RedisBungeeSpoof.addPlayer(bukkitPlayer, redis)
        }

        val ranks = RiftBukkitPlugin.instance.readSpoofRanks()
        if (ranks.isNotEmpty()) {
            val rank = Chance.weightedPick(ranks.keys.toList()) { ranks.getOrDefault(it, 0.0) }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ogrant ${bukkitPlayer.name} $rank 1d Buycraft (Rift)")

            if (DEBUG) {
                debugLog("Granted the $rank rank to ${player.name}")
            }
        }

        Tasks.sync {
            try {
                MinecraftServer.getServer().playerList.onPlayerJoin(player, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Tasks.delayed(40L) {
                val readCommands = RiftBukkitPlugin.instance.readSpoofActions()

                val toPerform = arrayListOf<String>()
                toPerform.addAll(readCommands.filter { it.second >= 100.0 }.map { it.first })
                toPerform.add(Chance.weightedPick(readCommands) { it.second }.first)

                for (command in toPerform) {
                    if (DEBUG) {
                        debugLog("Executing command $command for ${player.name}")
                    }

                    try {
                        player.bukkitEntity.performCommand(command)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun removeFakePlayer(player: FakeEntityPlayer, forceAsync: Boolean = true) {
        SpoofPlayerRemoveEvent(player).call()

        // de-spawn fake player THEN remove from map
        try {
            MinecraftServer.getServer().playerList.disconnect(player)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fakePlayers.remove(player.uniqueID)

        if (forceAsync) {
            Tasks.async {
                Cubed.instance.redis.runRedisCommand { redis ->
                    try {
                        RedisBungeeSpoof.removePlayer(player.bukkitEntity, redis)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            Cubed.instance.redis.runRedisCommand { redis ->
                try {
                    RedisBungeeSpoof.removePlayer(player.bukkitEntity, redis)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun nextRandomProfile(): JsonArray? {
        return if (profiles.size() == 0) {
            null
        } else {
            profiles[ThreadLocalRandom.current().nextInt(
                profiles.size())].asJsonArray
        }
    }

    private fun updatePings() {
        for (player in RiftBukkitPlugin.instance.server.onlinePlayers) {
            val nmsPlayer = (player as CraftPlayer).handle
            if (nmsPlayer is FakeEntityPlayer) {
                nmsPlayer.ping = max(min(ThreadLocalRandom.current().nextInt(nmsPlayer.ping - 5, nmsPlayer.ping + 5), 200), 20)
            }
        }
    }

    fun debugLog(message: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.isOp) {
                player.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}[DEBUG] ${ChatColor.RESET}$message")
            }
        }

        RiftBukkitPlugin.instance.logger.info(message)
    }

}