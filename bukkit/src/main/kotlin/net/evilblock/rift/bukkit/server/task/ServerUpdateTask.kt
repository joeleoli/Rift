package net.evilblock.rift.bukkit.server.task

import net.evilblock.cubed.util.nms.MinecraftReflection
import net.evilblock.rift.bukkit.RiftBukkitPlugin
import net.evilblock.rift.server.ServerHandler
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class ServerUpdateTask : BukkitRunnable() {

    override fun run() {
        val optionalServer = RiftBukkitPlugin.instance.getServerInstance()

        val server = if (optionalServer.isPresent) {
            optionalServer.get()
        } else {
            ServerHandler.loadOrCreateServer(RiftBukkitPlugin.instance.readServerId(), Bukkit.getPort())
        }

        server.slots = Bukkit.getMaxPlayers()
        server.whitelisted = Bukkit.hasWhitelist()
        server.onlineMode = Bukkit.getOnlineMode()
        server.proxied = RiftBukkitPlugin.instance.readBungeeEnabled()
        server.lastHeartbeat = System.currentTimeMillis()
        server.currentUptime = System.currentTimeMillis() - RiftBukkitPlugin.instance.enabledAt
        server.currentTps = MinecraftReflection.getTPS()
        server.playerCount = Bukkit.getOnlinePlayers().size

        ServerHandler.saveServer(server)
    }

}