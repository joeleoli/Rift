package com.minexd.rift.bukkit.server.listener

import com.minexd.rift.Rift
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.server.ServerHandler
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.pidgin.message.Message
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object ServerCountListeners : Listener {

    /**
     * Instantly broadcasts a player count update message to all listening Rift instances
     * when a player joins this server. Makes player counts real-time across the network.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        Tasks.async {
            Rift.instance.mainChannel.sendMessage(Message(
                id = ServerHandler.SERVER_UPDATE_COUNT,
                data = mapOf(
                    "Server" to RiftBukkitPlugin.instance.serverInstance.id,
                    "Count" to Bukkit.getServer().onlinePlayers.size
                )
            ))
        }
    }

}