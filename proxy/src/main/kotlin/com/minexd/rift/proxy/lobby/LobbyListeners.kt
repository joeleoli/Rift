package com.minexd.rift.proxy.lobby

import com.minexd.rift.proxy.RiftProxyPlugin
import com.minexd.rift.server.ServerHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object LobbyListeners : Listener {

    private val kickRegexes = arrayListOf("You have been kicked", "Server is restarting", "Server closed", "Internal Exception")

    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        val player = event.player
        val server = player.server
        val target = event.target

        val targetServer = ServerHandler.getServerById(target.name)
            ?: return

        if (server == null && targetServer.group == "MainLobbies") {
            val lobbyServer = findBestLobbyServer()
            if (lobbyServer != null) {
                event.target = lobbyServer
            }
        }
    }

    @EventHandler
    fun onServerKickEvent(event: ServerKickEvent) {
        val player = event.player
        val reason = TextComponent.toPlainText(*event.kickReasonComponent)
        val from = event.kickedFrom

        if (player.server == null) {
            return
        }

        if (from == player.server.info) {
            for (regex in kickRegexes) {
                if (reason.matches(regex.toRegex())) {
                    val lobbyServer = findBestLobbyServer()
                    if (lobbyServer != null) {
                        player.sendMessage("${ChatColor.GREEN}You've been connected to ${ChatColor.DARK_AQUA}${ChatColor.BOLD}${lobbyServer.name}${ChatColor.GREEN}!")
                        event.isCancelled = true
                        event.cancelServer = lobbyServer
                    }
                }
            }
        }
    }

    private fun findBestLobbyServer(): ServerInfo? {
        val mainLobbies = ServerHandler.getGroupById("MainLobbies")
            ?: return null

        val availableLobbies = mainLobbies.servers.filter {
            it.isOnline()
                    && !it.whitelisted
                    && !it.isServerFull()
                    && RiftProxyPlugin.instance.proxy.getServerInfo(it.id) != null
        }

        if (availableLobbies.isEmpty()) {
            return null
        }

        val lobbyServer = availableLobbies.minBy { it.playerCount }
            ?: return null

        return RiftProxyPlugin.instance.proxy.getServerInfo(lobbyServer.id)
    }

}