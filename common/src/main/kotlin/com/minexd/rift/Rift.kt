package com.minexd.rift

import com.minexd.rift.server.ServerMessages
import com.minexd.rift.server.ServerHandler
import net.evilblock.pidgin.Pidgin
import net.evilblock.pidgin.PidginOptions
import com.minexd.rift.plugin.Plugin
import com.minexd.rift.proxy.ProxyHandler
import com.minexd.rift.proxy.ProxyMessages
import com.minexd.rift.queue.QueueHandler
import com.minexd.rift.queue.QueueMessages
import net.evilblock.cubed.serializers.Serializers

class Rift(val plugin: Plugin) {

    companion object {
        @JvmStatic lateinit var instance: Rift
    }

    lateinit var mainChannel: Pidgin
    lateinit var proxyChannel: Pidgin

    init {
        instance = this
    }

    fun initialLoad() {
        mainChannel = Pidgin("Rift-Main", plugin.getRedis().jedisPool!!, Serializers.gson, PidginOptions(async = true))

        ProxyHandler.initialLoad()
        ServerHandler.initialLoad()
        QueueHandler.initialLoad()

        mainChannel.registerListener(ProxyMessages)
        mainChannel.registerListener(ServerMessages)
        mainChannel.registerListener(QueueMessages)

        proxyChannel = Pidgin("Rift-Proxy", plugin.getRedis().jedisPool!!, Serializers.gson, PidginOptions(async = true))
    }

}