package net.evilblock.rift.bukkit.spoof.listener

import net.evilblock.rift.bukkit.spoof.SpoofHandler
import net.evilblock.rift.bukkit.spoof.thread.SpoofThread
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object SpoofListeners : Listener {

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (!SpoofHandler.isFakePlayer(event.player)) {
            if (SpoofThread.nextChange < System.currentTimeMillis() + 4000L) {
                SpoofThread.nextChange = System.currentTimeMillis() + 4000L
            }
        }
    }

}