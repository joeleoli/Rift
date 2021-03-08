package com.minexd.rift.proxy

import com.google.gson.JsonObject
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener

object ProxyMessages : MessageListener {

    @IncomingMessageHandler(ProxyHandler.PROXY_UPDATE)
    fun onProxyUpdate(data: JsonObject) {
        ProxyHandler.loadOrCreateProxy(data["Proxy"].asString)
    }

}