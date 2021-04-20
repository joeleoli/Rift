package com.minexd.rift.proxy.queue

import com.google.gson.JsonObject
import com.minexd.rift.proxy.RiftProxyPlugin
import com.minexd.rift.queue.QueueHandler
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import java.util.*

object ProxyQueueMessages : MessageListener {

    @IncomingMessageHandler(QueueHandler.QUEUE_POLL)
    fun onQueuePoll(data: JsonObject) {
        val player = RiftProxyPlugin.instance.proxy.getPlayer(UUID.fromString(data["Player"].asString)) ?: return
        val route = RiftProxyPlugin.instance.proxy.getServerInfo(data["Route"].asString) ?: return

        player.sendMessage(*ComponentBuilder("Queue > ")
            .color(ChatColor.GOLD)
            .bold(true)
            .append("You're up! Sending you to ")
            .color(ChatColor.GRAY)
            .bold(false)
            .append(route.name)
            .color(ChatColor.YELLOW)
            .bold(true)
            .append("...")
            .color(ChatColor.GRAY)
            .bold(false)
            .create())

        player.connect(route)
    }

}