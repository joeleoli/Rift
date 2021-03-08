package com.minexd.rift.bukkit.server.task

import net.evilblock.cubed.util.nms.MinecraftReflection
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.server.ServerHandler
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class ServerUpdateTask : BukkitRunnable() {

    override fun run() {
        val server = RiftBukkitPlugin.instance.serverInstance

        server.slots = Bukkit.getMaxPlayers()
        server.whitelisted = Bukkit.hasWhitelist()
        server.onlineMode = Bukkit.getOnlineMode()
        server.proxied = RiftBukkitPlugin.instance.readBungeeEnabled()
        server.lastHeartbeat = System.currentTimeMillis()
        server.currentUptime = System.currentTimeMillis() - RiftBukkitPlugin.enabledAt
        server.currentTps = MinecraftReflection.getTPS()
        server.playerCount = Bukkit.getOnlinePlayers().size

        ServerHandler.saveServer(server)
    }

}