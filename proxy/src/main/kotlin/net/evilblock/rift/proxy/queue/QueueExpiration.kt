package net.evilblock.rift.proxy.queue

import net.evilblock.rift.proxy.RiftProxyPlugin
import net.evilblock.rift.queue.QueueEntry
import net.evilblock.rift.queue.QueueHandler
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.concurrent.TimeUnit

object QueueExpiration : Runnable, Listener {

    override fun run() {
        for (queue in QueueHandler.getQueues()) {
            val toRemove = arrayListOf<QueueEntry>()

            for (entry in queue.cachedEntries) {
                // redundancy check, just in case proxy crashes and doesn't handle logout events of players
                val player = RiftProxyPlugin.instance.proxy.getPlayer(entry.uuid)
                if (player == null && entry.expiresAt == null) {
                    entry.expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5L)
                }

                if (entry.expiresAt != null && System.currentTimeMillis() >= entry.expiresAt!!) {
                    toRemove.add(entry)
                }
            }

            for (entry in toRemove) {
                queue.removeEntry(entry)
            }
        }
    }

    @EventHandler
    fun onLoginEvent(event: PostLoginEvent) {
        val queue = QueueHandler.getQueueByEntry(event.player.uniqueId)
        if (queue != null) {
            val entry = queue.getEntry(event.player.uniqueId)
            if (entry != null) {
                entry.expiresAt = null
            }
        }
    }

    @EventHandler
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        val queue = QueueHandler.getQueueByEntry(event.player.uniqueId)
        if (queue != null) {
            val entry = queue.getEntry(event.player.uniqueId)
            if (entry != null) {
                entry.expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)
            }
        }
    }

}