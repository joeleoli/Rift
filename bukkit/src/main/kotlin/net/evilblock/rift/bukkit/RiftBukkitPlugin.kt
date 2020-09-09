package net.evilblock.rift.bukkit

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.CubedOptions
import net.evilblock.cubed.command.CommandHandler
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.rift.Rift
import net.evilblock.rift.bukkit.queue.command.QueueJoinCommand
import net.evilblock.rift.server.Server
import net.evilblock.rift.server.ServerHandler
import net.evilblock.rift.bukkit.server.command.ServerDumpCommand
import net.evilblock.rift.bukkit.queue.command.QueueEditorCommand
import net.evilblock.rift.bukkit.queue.command.QueueLeaveCommand
import net.evilblock.rift.bukkit.queue.command.parameter.QueueParameterType
import net.evilblock.rift.bukkit.queue.event.PlayerJoinQueueEvent
import net.evilblock.rift.bukkit.queue.event.PlayerLeaveQueueEvent
import net.evilblock.rift.bukkit.server.task.ServerUpdateTask
import net.evilblock.rift.plugin.Plugin
import net.evilblock.rift.queue.Queue
import net.evilblock.rift.queue.QueueEntry
import net.evilblock.rift.queue.QueueHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.JedisPool
import java.io.File
import java.util.*

class RiftBukkitPlugin : JavaPlugin(), Plugin {

    companion object {
        @JvmStatic
        lateinit var instance: RiftBukkitPlugin
    }

    val enabledAt = System.currentTimeMillis()

    override fun onEnable() {
        instance = this

        Rift(this).initialLoad()

        // setup our configuration, and stop the onEnable process if we've generated a config for the first time
        // this allows the server operator a chance to configure their Rift instance before data is mutated
        if (!setupConfiguration()) {
            return
        }

        Cubed.instance.configureOptions(CubedOptions(requireRedis = true))

        CommandHandler.registerClass(QueueEditorCommand.javaClass)
        CommandHandler.registerClass(QueueJoinCommand.javaClass)
        CommandHandler.registerClass(QueueLeaveCommand.javaClass)
        CommandHandler.registerClass(ServerDumpCommand.javaClass)
        CommandHandler.registerParameterType(Queue::class.java, QueueParameterType)

        // save and broadcast this server's data on an interval defined in the config
        Tasks.asyncTimer(ServerUpdateTask(), 20L, 20L * readBroadcastInterval())
    }

    private fun setupConfiguration(): Boolean {
        // we're going to use this later to disable the server if we're generating the default config file
        val configExists = File(dataFolder, "config.yml").exists()

        // save default config
        saveDefaultConfig()

        // disable the server after we save the default config, to prevent data from being overwritten
        if (!configExists) {
            logger.info("***************************************************")
            logger.info("                   IMPORTANT")
            logger.info("Rift has generated the default configuration.")
            logger.info("To allow you, the server operator, a chance to")
            logger.info("configure your Rift instance, the server has")
            logger.info("been shutdown. Next server startup will execute")
            logger.info("as normal.")
            logger.info("***************************************************")
            shutdownServer()
            return false
        }

        return true
    }

    private fun shutdownServer() {
        server.pluginManager.disablePlugin(this)
        server.shutdown()
    }

    fun readServerId(): String {
        return config.getString("instance.server-id")
    }

    private fun readBroadcastInterval(): Int {
        return config.getInt("broadcast-update-interval")
    }

    private fun readDisableQueues(): Boolean {
        return config.getBoolean("disable-queues")
    }

    fun readDisableJoinQueueCommand(): Boolean {
        return config.getBoolean("disable-join-queue-command")
    }

    fun readBungeeEnabled(): Boolean {
        return Reflection.getDeclaredField(Reflection.getClassSuppressed("org.spigotmc.SpigotConfig")!!, "bungee")!!.get(null) as Boolean
    }

    fun getServerInstance(): Optional<Server> {
        return ServerHandler.getServerById(readServerId())
    }

    private fun findPriority(player: Player): Int {
        return QueueHandler.getPriority().entries.sortedBy { it.value }.reversed().firstOrNull() { player.hasPermission(it.key) }?.value ?: 0
    }

    fun joinQueue(player: Player, queue: Queue) {
        if (getServerInstance().get().id == queue.route.id) {
            player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}You can't join a queue for a server you're already connected to.")
            return
        }

        if (readDisableQueues()) {
            player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}You can't join queues from this server.")
            return
        }

        val currentQueue = QueueHandler.getQueueByEntry(player.uniqueId)
        if (currentQueue != null) {
            player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}You are already in the ${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}${currentQueue.route.displayName} ${ChatColor.RED}queue.")
            return
        }

        if (!queue.open) {
            player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}That queue is currently closed.")
            return
        }

        queue.addEntry(player.uniqueId, findPriority(player))
    }

    override fun getJedisPool(): JedisPool {
        return Cubed.instance.redis.jedisPool!!
    }

    override fun isProxy(): Boolean {
        return false
    }

    override fun onJoinQueue(queue: Queue, entry: QueueEntry) {
        Bukkit.getPlayer(entry.uuid).let {
            if (it != null) {
                Tasks.sync {
                    PlayerJoinQueueEvent(it, queue).call()
                }
            }
        }
    }

    override fun onLeaveQueue(queue: Queue, entry: QueueEntry) {
        Bukkit.getPlayer(entry.uuid).let {
            if (it != null) {
                Tasks.sync {
                    PlayerLeaveQueueEvent(it, queue).call()
                }
            }
        }
    }

}