package com.minexd.rift.proxy.queue.task

import com.minexd.rift.proxy.RiftProxyPlugin
import com.minexd.rift.queue.QueueHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object QueuePollTask : Runnable {

    var iterations = 0

    override fun run() {
        if (iterations++ >= 80) {
            iterations = 0

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

        for (queue in QueueHandler.getQueues()) {
            val routeInfo = RiftProxyPlugin.instance.proxy.getServerInfo(queue.route.id) ?: continue

            if (queue.canPoll()) {
                queue.lastPoll = System.currentTimeMillis()

                val entries = queue.getSortedEntries()
                var sent = 0

                val iterator = entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val player = RiftProxyPlugin.instance.proxy.getPlayer(entry.uuid) ?: continue

                    iterator.remove()
                    queue.removeEntry(entry)

                    sent++

                    player.sendMessage(*ComponentBuilder("QUEUE ")
                        .color(ChatColor.RED)
                        .bold(true)
                        .append("You're up! Sending you to ")
                        .color(ChatColor.GRAY)
                        .bold(false)
                        .append(queue.route.displayName)
                        .color(ChatColor.LIGHT_PURPLE)
                        .bold(true)
                        .append("...")
                        .color(ChatColor.GRAY)
                        .bold(false)
                        .create())

                    player.connect(routeInfo)

                    if (sent >= queue.pollingSize) {
                        break
                    }
                }
            }
        }
    }

}