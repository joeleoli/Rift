package com.minexd.rift.bukkit.spoof.store

import com.google.gson.Gson
import net.evilblock.cubed.Cubed
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.SpoofHandler
import org.bukkit.entity.Player
import redis.clients.jedis.Jedis
import java.util.*

object RedisBungeeSpoof {

    private val gson: Gson = Gson()

    @JvmStatic
    fun addPlayer(player: Player, redis: Jedis) {
        if (Cubed.instance.uuidCache.uuid(player.name) == null) {
            Cubed.instance.uuidCache.update(player.uniqueId, player.name)
        }

        val playerData = mapOf(
            "online" to "0",
            "ip" to player.address.address.hostAddress,
            "server" to RiftBukkitPlugin.instance.readServerId(),
            "proxy" to SpoofHandler.getSpoofedProxyId()
        )

        redis.sadd("proxy:${SpoofHandler.getSpoofedProxyId()}:usersOnline", player.uniqueId.toString())
        redis.hmset("player:${player.uniqueId}", playerData)
    }

    @JvmStatic
    fun removePlayer(player: Player, redis: Jedis) {
        redis.srem("proxy:${SpoofHandler.getSpoofedProxyId()}:usersOnline", player.uniqueId.toString())
        redis.hdel("player:${player.uniqueId}", "server", "ip", "proxy")
        redis.hset("player:${player.uniqueId}", "online", System.currentTimeMillis().toString())
    }

    @JvmStatic
    fun cachePlayer(player: Player, redis: Jedis) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 3)

        val entry = CachedUUIDEntry(
            player.name,
            player.uniqueId,
            calendar
        )
        val json = gson.toJson(entry)

        redis.hmset("uuid-cache", mapOf(player.name.toLowerCase() to json, player.uniqueId.toString() to json))
    }

    @JvmStatic
    fun cleanupLaggedPlayers(redis: Jedis) {
        var fixed = 0
        for (player in redis.smembers("proxy:${SpoofHandler.getSpoofedProxyId()}:usersOnline")) {
            val playerData = redis.hgetAll("player:$player")
            if (playerData.containsKey("server")) {
                if (playerData["server"]!! == RiftBukkitPlugin.instance.readServerId()) {
                    fixed++
                    redis.srem("proxy:${SpoofHandler.getSpoofedProxyId()}:usersOnline", player.toString())
                    redis.hdel("player:$player", "server", "ip", "proxy")
                    redis.hset("player:$player", "online", System.currentTimeMillis().toString())
                }
            }
        }

        if (fixed > 0) {
            RiftBukkitPlugin.instance.logger.info("Fixed $fixed GS players!")
        }
    }

    class CachedUUIDEntry(val name: String, val uuid: UUID, val expiry: Calendar) {
        fun isExpired(): Boolean {
            return Calendar.getInstance().after(expiry)
        }
    }

}