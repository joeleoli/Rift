package net.evilblock.rift.proxy

import com.google.common.io.ByteStreams
import net.evilblock.rift.Rift
import net.evilblock.rift.proxy.queue.QueueExpiration
import net.evilblock.rift.proxy.queue.task.QueuePollTask
import net.evilblock.rift.queue.Queue
import net.evilblock.rift.queue.QueueEntry
import net.evilblock.rift.server.ServerHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.lang.StringBuilder
import java.net.URI
import java.util.concurrent.TimeUnit

class RiftProxyPlugin : Plugin(), net.evilblock.rift.plugin.Plugin {

    companion object {
        @JvmStatic
        lateinit var instance: RiftProxyPlugin
    }

    private lateinit var configuration: Configuration
    private lateinit var jedisPool: JedisPool

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        loadConfig()
        loadJedis()

        Rift(this).initialLoad()

        for (server in proxy.servers.values) {
            ServerHandler.loadOrCreateServer(server.name, server.address.port)
        }

        proxy.pluginManager.registerListener(this, QueueExpiration)

        proxy.scheduler.schedule(this, QueuePollTask, 250L, 250L, TimeUnit.MILLISECONDS)
        proxy.scheduler.schedule(this, QueueExpiration, 1L, 1L, TimeUnit.SECONDS)
    }

    private fun loadConfig() {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))
    }

    private fun saveDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            try {
                configFile.createNewFile()

                getResourceAsStream("config.yml")
                    .use { `is` -> FileOutputStream(configFile)
                    .use { os -> ByteStreams.copy(`is`, os) } }
            } catch (e: Throwable) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    private fun loadJedis() {
        val redisHost = configuration.getString("redis.host")
        val redisPort = configuration.getInt("redis.port")
        val redisPassword = configuration.getString("redis.password")
        val redisDbId = configuration.getInt("redis.dbId")

        try {
            val password = if (redisPassword != null && redisPassword.isNotEmpty()) {
                redisPassword
            } else {
                null
            }

            jedisPool = JedisPool(JedisPoolConfig(), redisHost, redisPort, 5000, password, redisDbId)

            if (password != null) {
                try {
                    jedisPool.resource.use { redis ->
                        redis.auth(redisPassword)
                    }
                } catch (e: Exception) {
                    throw RuntimeException("Could not authenticate", e)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Couldn't connect to redis server at ${redisHost}:${redisPort}", e)
        }
    }

    override fun getJedisPool(): JedisPool {
        return jedisPool
    }

    override fun isProxy(): Boolean {
        return true
    }

    override fun onJoinQueue(queue: Queue, entry: QueueEntry) {
        val player = proxy.getPlayer(entry.uuid) ?: return

        player.sendMessage(*ComponentBuilder("QUEUE ")
            .color(ChatColor.RED)
            .bold(true)
            .append("You've joined the ")
            .color(ChatColor.GRAY)
            .bold(false)
            .append(queue.route.displayName)
            .color(ChatColor.LIGHT_PURPLE)
            .bold(true)
            .append(" queue at position ")
            .color(ChatColor.GRAY)
            .bold(false)
            .append("#${entry.position}")
            .color(ChatColor.LIGHT_PURPLE)
            .bold(true)
            .append("...")
            .color(ChatColor.GRAY)
            .bold(false)
            .create())

        player.sendMessage(*ComponentBuilder("QUEUE ")
            .color(ChatColor.RED)
            .bold(true)
            .append("If you disconnect, you will have 5 minutes to reconnect before you're removed from the queue.")
            .color(ChatColor.GRAY)
            .bold(false)
            .create())
    }

    override fun onLeaveQueue(queue: Queue, entry: QueueEntry) {
        val player = proxy.getPlayer(entry.uuid) ?: return

        player.sendMessage(*ComponentBuilder("QUEUE ")
            .color(ChatColor.RED)
            .bold(true)
            .append("You've been removed from the ")
            .color(ChatColor.GRAY)
            .bold(false)
            .append(queue.route.displayName)
            .color(ChatColor.LIGHT_PURPLE)
            .bold(true)
            .append(" queue.")
            .color(ChatColor.GRAY)
            .bold(false)
            .create())
    }

}