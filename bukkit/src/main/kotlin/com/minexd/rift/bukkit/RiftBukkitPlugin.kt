package com.minexd.rift.bukkit

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.CommandHandler
import net.evilblock.cubed.store.redis.Redis
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.rift.Rift
import com.minexd.rift.bukkit.command.DebugCommand
import com.minexd.rift.bukkit.jump.LobbyCommand
import com.minexd.rift.bukkit.command.ReloadCommand
import com.minexd.rift.bukkit.jump.SendAllFromToCommand
import com.minexd.rift.bukkit.jump.SendAllToCommand
import com.minexd.rift.bukkit.queue.command.QueueJoinCommand
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import com.minexd.rift.bukkit.queue.command.QueueEditorCommand
import com.minexd.rift.bukkit.queue.command.QueueLeaveCommand
import com.minexd.rift.bukkit.queue.command.parameter.QueueParameterType
import com.minexd.rift.bukkit.queue.event.PlayerJoinQueueEvent
import com.minexd.rift.bukkit.queue.event.PlayerLeaveQueueEvent
import com.minexd.rift.bukkit.server.command.*
import com.minexd.rift.bukkit.server.command.parameter.ServerParameterType
import com.minexd.rift.bukkit.server.listener.ServerCountListeners
import com.minexd.rift.bukkit.server.task.ServerUpdateTask
import com.minexd.rift.bukkit.spoof.SpoofHandler
import com.minexd.rift.bukkit.spoof.command.*
import com.minexd.rift.bukkit.util.Constants
import com.minexd.rift.plugin.Plugin
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueEntry
import com.minexd.rift.queue.QueueHandler
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RiftBukkitPlugin : JavaPlugin(), Plugin {

    companion object {
        @JvmStatic
        lateinit var instance: RiftBukkitPlugin

        @JvmStatic
        val enabledAt: Long = System.currentTimeMillis()
    }

    lateinit var serverInstance: Server

    override fun onEnable() {
        instance = this

        try {
            if (!setupConfiguration()) {
                return
            }

            Rift(this).initialLoad()

            serverInstance = ServerHandler.loadOrCreateServer(readServerId(), server.port)

            loadCommands()
            loadListeners()

            SpoofHandler.initialLoad()

            Tasks.asyncTimer(ServerUpdateTask(), 20L, 20L * readBroadcastInterval())
        } catch (e: Exception) {
            e.printStackTrace()
            shutdownServer()
        }
    }

    override fun reloadConfig() {
        super.reloadConfig()

        serverInstance = ServerHandler.loadOrCreateServer(readServerId(), server.port)
    }

    override fun onDisable() {
        SpoofHandler.onDisable()
    }

    override fun getDirectory(): File {
        return dataFolder
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

    private fun loadCommands() {
        CommandHandler.registerParameterType(Server::class.java, ServerParameterType())
        CommandHandler.registerParameterType(Queue::class.java, QueueParameterType())

        CommandHandler.registerClass(DebugCommand.javaClass)
        CommandHandler.registerClass(ReloadCommand.javaClass)

        CommandHandler.registerClass(LobbyCommand.javaClass)
        CommandHandler.registerClass(SendAllToCommand.javaClass)
        CommandHandler.registerClass(SendAllFromToCommand.javaClass)

        CommandHandler.registerClass(ServersCommand.javaClass)
        CommandHandler.registerClass(ServerGroupsCommand.javaClass)
        CommandHandler.registerClass(ServerDumpCommand.javaClass)
        CommandHandler.registerClass(ServerJumpCommand.javaClass)
        CommandHandler.registerClass(ServerMetadataEditorCommand.javaClass)

        CommandHandler.registerClass(QueueEditorCommand.javaClass)
        CommandHandler.registerClass(QueueJoinCommand.javaClass)
        CommandHandler.registerClass(QueueLeaveCommand.javaClass)

        CommandHandler.registerClass(SpoofDebugCommand.javaClass)
        CommandHandler.registerClass(SpoofMinCommand.javaClass)
        CommandHandler.registerClass(SpoofMaxCommand.javaClass)
        CommandHandler.registerClass(SpoofMinDelayCommand.javaClass)
        CommandHandler.registerClass(SpoofMaxDelayCommand.javaClass)
        CommandHandler.registerClass(SpoofMultiCommand.javaClass)
        CommandHandler.registerClass(SpoofPauseCommand.javaClass)
        CommandHandler.registerClass(SpoofStatusCommand.javaClass)
        CommandHandler.registerClass(SpoofToggleCommand.javaClass)
        CommandHandler.registerClass(SpoofToggleCommand.javaClass)
    }

    private fun loadListeners() {
        server.pluginManager.also { pm ->
            pm.registerEvents(ServerCountListeners, this)
        }
    }

    fun readProxyId(): String {
        return config.getString("instance.proxy-id")
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


    fun readSpoofEnabled(): Boolean {
        return config.getBoolean("spoof.enabled", false)
    }

    fun readSpoofPaused(): Boolean {
        return config.getBoolean("spoof.paused", false)
    }

    fun readSpoofMultiplier(): Double {
        return config.getDouble("spoof.multiplier", 2.0)
    }

    fun readSpoofReact(): Boolean {
        return config.getBoolean("spoof.react", false)
    }

    fun setSpoofMultiplier(value: Double) {
        config.set("spoof.multiplier", value)
        saveConfig()
    }

    fun readSpoofMin(): Int {
        return config.getInt("spoof.min", 10)
    }

    fun setSpoofMin(value: Int) {
        config.set("spoof.min", value)
        saveConfig()
    }

    fun readSpoofMax(): Int {
        return config.getInt("spoof.max", 100)
    }

    fun setSpoofMax(value: Int) {
        config.set("spoof.max", value)
        saveConfig()
    }

    fun readSpoofBuffer(): Int {
        return config.getInt("spoof.buffer", 25)
    }

    fun readSpoofInterval(): Long {
        return config.getLong("spoof.interval", 20)
    }

    fun readSpoofMinDelay(): Long {
        return config.getLong("spoof.min-delay", 500L)
    }

    fun setSpoofMinDelay(minDelay: Long) {
        config.set("spoof.min-delay", minDelay)
        saveConfig()
    }

    fun readSpoofMaxDelay(): Long {
        return config.getLong("spoof.max-delay", 1500L)
    }

    fun setSpoofMaxDelay(maxDelay: Long) {
        config.set("spoof.max-delay", maxDelay)
        saveConfig()
    }

    fun readSpoofStabilize(): Boolean {
        return config.getBoolean("spoof.stabilize", true)
    }

    fun readSpoofRanks(): Map<String, Double> {
        return if (config.contains("spoof.realism.rank-assignment")) {
            hashMapOf<String, Double>().also {
                val section = config.getConfigurationSection("spoof.realism.rank-assignment")
                for (key in section.getKeys(false)) {
                    it[key] = section.getDouble(key)
                }
            }
        } else {
            emptyMap()
        }
    }

    fun readSpoofActions(): List<Pair<String, Double>> {
        return if (config.contains("spoof.realism.actions")) {
            arrayListOf<Pair<String, Double>>().also {
                for (map in config.getList("spoof.realism.actions") as List<Map<String, Any>>) {
                    it.add(Pair(map["command"] as String, map["chance"] as Double))
                }
            }
        } else {
            emptyList()
        }
    }

    private fun findPriority(player: Player): Int {
        return QueueHandler.getPriority().entries.sortedBy { it.value }.reversed().firstOrNull() { player.hasPermission(it.key) }?.value ?: 0
    }

    fun joinQueue(player: Player, queue: Queue) {
        if (player.isOp || player.hasPermission(com.minexd.rift.bukkit.util.Permissions.JUMP_SERVER)) {
            player.sendMessage("${Constants.QUEUE_CHAT_PREFIX}${ChatColor.GRAY}Sending you to ${ChatColor.YELLOW}${queue.route.displayName}${ChatColor.GRAY}!")
            BungeeUtil.sendToServer(player, queue.route.id)
            return
        }

        if (serverInstance.id == queue.route.id) {
            player.sendMessage("${ChatColor.RED}You're already connected to that server!")
            return
        }

        if (readDisableQueues()) {
            player.sendMessage("${ChatColor.RED}You can't join queues from this server!")
            return
        }

        val currentQueue = QueueHandler.getQueueByEntry(player.uniqueId)
        if (currentQueue != null) {
            player.sendMessage("${ChatColor.RED}You are already in the ${queue.route.getColor()}${queue.route.displayName} ${ChatColor.RED}queue!")
            return
        }

        if (!queue.open) {
            player.sendMessage("${ChatColor.RED}That queue is currently closed!")
            return
        }

        queue.addEntry(player.uniqueId, findPriority(player))
    }

    override fun getRedis(): Redis {
        return Cubed.instance.redis
    }

    override fun hasPresence(): Boolean {
        return true
    }

    override fun getInstanceID(): String {
        return serverInstance.id
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