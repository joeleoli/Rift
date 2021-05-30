package com.minexd.rift.proxy.proxy

import com.minexd.rift.server.ServerHandler
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object ProxyListeners : Listener {

    @EventHandler
    fun onProxyPingEvent(event: ProxyPingEvent) {
        val serverPing = event.response
        serverPing.players = ServerPing.Players(1000, ServerHandler.getOnlinePlayerCount(), arrayOf())
    }

}