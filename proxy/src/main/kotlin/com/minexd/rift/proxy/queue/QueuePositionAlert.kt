package com.minexd.rift.proxy.queue

import com.minexd.rift.proxy.RiftProxyPlugin
import com.minexd.rift.queue.QueueHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object QueuePositionAlert : Runnable {

    override fun run() {
        for (queue in QueueHandler.getQueues()) {
            for (entry in queue.cachedEntries) {
                val player = RiftProxyPlugin.instance.proxy.getPlayer(entry.uuid) ?: continue

                player.sendMessage(*ComponentBuilder("QUEUE ")
                    .color(ChatColor.RED)
                    .bold(true)
                    .append("You're in the ")
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
                    .append(".")
                    .color(ChatColor.GRAY)
                    .bold(false)
                    .create())
            }
        }
    }

}