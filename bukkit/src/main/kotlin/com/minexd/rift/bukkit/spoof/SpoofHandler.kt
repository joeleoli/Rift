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
import com.minexd.rift.queue.QueueHandler
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

    const val ADD_SPOOFED_PLAYER = "AddSpoofedPlayer"
    const val REMOVE_SPOOFED_PLAYER = "RemoveSpoofedPlayer"
    const val SWITCH_SPOOFED_PLAYER = "SwitchSpoofedPlayer"

    var DEBUG: Boolean = false

    private var enabled: Boolean = false
    private var paused: Boolean = false

    private var profiles: MutableMap<UUID, JsonArray> = ConcurrentHashMap()
    private var fakePlayers: ConcurrentHashMap<UUID, FakeEntityPlayer> = ConcurrentHashMap()

    private var tasks: MutableList<BukkitTask> = arrayListOf()

    fun initialLoad() {
        enabled = RiftBukkitPlugin.instance.readSpoofEnabled()
        paused = RiftBukkitPlugin.instance.readSpoofPaused()

        if (enabled || RiftBukkitPlugin.instance.readLoadSpoofProfiles()) {
            profiles = loadProfiles()
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
            profiles = loadProfiles()
        } else {
            profiles = ConcurrentHashMap()

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

    private fun loadProfiles(): ConcurrentHashMap<UUID, JsonArray> {
        return ConcurrentHashMap<UUID, JsonArray>().also { map ->
            val profilesFile = File(Rift.instance.plugin.getDirectory(), "profiles.json")
            val profilesJson = Serializers.gson.fromJson(FileReader(profilesFile), JsonArray::class.java).map { it.asJsonArray }

            for (element in profilesJson) {
                map[UUID.fromString(element[1].asString)] = element
            }
        }
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
        return fakePlayers.contains(player.uniqueId) || (player as CraftPlayer).handle is FakeEntityPlayer
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

            val readCommands = RiftBukkitPlugin.instance.readSpoofActions()
            if (readCommands.isNotEmpty()) {
                Tasks.delayed(40L) {
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

            val queueList = RiftBukkitPlugin.instance.readSpoofedQueues()
            if (queueList.isNotEmpty()) {
                val randomQueueId = Chance.weightedPick(queueList) { it.second }.first
                println("random queue ID: $randomQueueId")

                val randomQueue = QueueHandler.getQueueById(randomQueueId)
                if (randomQueue != null) {
                    Tasks.asyncDelayed(40L) {
                        println("forced ${player.bukkitEntity.name} to join ${randomQueue.id}")
                        RiftBukkitPlugin.instance.joinQueue(player.bukkitEntity, randomQueue, true)
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

    fun areFakeProfilesLoaded(): Boolean {
        return profiles.isNotEmpty()
    }

    fun getFakeProfile(uuid: UUID): JsonArray? {
        return profiles[uuid]
    }

    fun nextRandomProfile(): JsonArray? {
        return if (profiles.isEmpty()) {
            null
        } else {
            profiles.values.random()
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