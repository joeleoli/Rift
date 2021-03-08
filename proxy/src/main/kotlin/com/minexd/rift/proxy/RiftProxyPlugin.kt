package com.minexd.rift.proxy

import com.google.common.io.ByteStreams
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.store.redis.Redis
import com.minexd.rift.Rift
import com.minexd.rift.proxy.queue.QueueExpiration
import com.minexd.rift.proxy.queue.task.QueuePollTask
import com.minexd.rift.proxy.task.ProxyUpdateTask
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueEntry
import com.minexd.rift.server.ServerHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class RiftProxyPlugin : Plugin(), com.minexd.rift.plugin.Plugin {

    companion object {
        @JvmStatic
        lateinit var instance: RiftProxyPlugin

        @JvmStatic
        val enabledAt: Long = System.currentTimeMillis()
    }

    lateinit var configuration: Configuration
    lateinit var proxyInstance: Proxy

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        saveDefaultConfig()
        loadConfig()

        Rift(this).initialLoad()

        try {
            proxyInstance = ProxyHandler.loadOrCreateProxy(readProxyId())

            for (server in proxy.servers.values) {
                ServerHandler.loadOrCreateServer(server.name, server.address.port)
            }

            proxy.pluginManager.registerListener(this, QueueExpiration)

            proxy.scheduler.schedule(this, QueuePollTask, 250L, 250L, TimeUnit.MILLISECONDS)
            proxy.scheduler.schedule(this, QueueExpiration, 1L, 1L, TimeUnit.SECONDS)
            proxy.scheduler.schedule(this, ProxyUpdateTask, 1L, readBroadcastInterval().toLong(), TimeUnit.SECONDS)
        } catch (e: Exception) {
            shutdownProxy()
        }
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

                getResourceAsStream("bungee_config.yml").use { input ->
                    FileOutputStream(configFile).use { output ->
                        ByteStreams.copy(input, output)
                    }
                }
            } catch (e: Throwable) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    private fun shutdownProxy() {
        proxy.stop("Failed to load Rift!")
    }

    fun readProxyId(): String {
        return configuration.getString("instance.proxy-id")
    }

    private fun readBroadcastInterval(): Int {
        return configuration.getInt("broadcast-update-interval")
    }

    override fun getDirectory(): File {
        return dataFolder
    }

    override fun getRedis(): Redis {
        return Cubed.instance.redis
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