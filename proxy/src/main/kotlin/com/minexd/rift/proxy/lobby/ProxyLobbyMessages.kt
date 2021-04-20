package com.minexd.rift.proxy.lobby

import com.google.gson.JsonObject
import com.minexd.rift.proxy.RiftProxyPlugin
import com.minexd.rift.server.ServerHandler
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.md_5.bungee.api.ChatColor
import java.util.*

object ProxyLobbyMessages : MessageListener {

    @IncomingMessageHandler("SendToLobby")
    fun onSendToLobby(data: JsonObject) {
        val players = when {
            data.has("Player") -> {
                listOf(RiftProxyPlugin.instance.proxy.getPlayer(UUID.fromString(data["Player"].asString)))
            }
            data.has("Players") -> {
                data["Players"].asJsonArray.map { RiftProxyPlugin.instance.proxy.getPlayer(UUID.fromString(it.asString)) }
            }
            else -> {
                return
            }
        }

        if (players.isEmpty()) {
            return
        }

        val mainLobbies = ServerHandler.getGroupById("MainLobbies")
        if (mainLobbies == null) {
            for (player in players) {
                player.sendMessage("${ChatColor.RED}Couldn't find any lobbies!")
            }
            return
        }

        val availableLobbies = mainLobbies.servers.filter {
            it.isOnline()
                    && !it.whitelisted
                    && !it.isServerFull()
                    && RiftProxyPlugin.instance.proxy.getServerInfo(it.id) != null
        }

        if (availableLobbies.isEmpty()) {
            for (player in players) {
                player.sendMessage("${ChatColor.RED}There are no available lobbies!")
            }
            return
        }

        for (player in players) {
            val lobbyServer = availableLobbies.minBy { it.playerCount } ?: continue
            val lobbyServerInfo = RiftProxyPlugin.instance.proxy.getServerInfo(lobbyServer.id) ?: continue

            player.connect(lobbyServerInfo)
        }
    }

    @IncomingMessageHandler("SendAllFromTo")
    fun onSendAllFromTo(data: JsonObject) {
        val from = RiftProxyPlugin.instance.proxy.getServerInfo(data["From"].asString)
            ?: return

        val to = RiftProxyPlugin.instance.proxy.getServerInfo(data["To"].asString)
            ?: return

        if (from.players.isEmpty()) {
            return
        }

        val exclude = data["Exclude"].asBoolean

        for (player in from.players) {
            if (exclude && player.hasPermission("rift.jump.all.exclude")) {
                continue
            }

            player.sendMessage("${ChatColor.GREEN}You've been sent to ${ChatColor.DARK_AQUA}${ChatColor.BOLD}${to.name}${ChatColor.GREEN}!")
            player.connect(to)
        }
    }

}