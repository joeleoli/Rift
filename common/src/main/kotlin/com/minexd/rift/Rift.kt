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

    lateinit var pidgin: Pidgin

    init {
        instance = this
    }

    fun initialLoad() {
        pidgin = Pidgin("Rift", plugin.getRedis().jedisPool!!, Serializers.gson, PidginOptions(async = true))

        ProxyHandler.initialLoad()
        ServerHandler.initialLoad()
        QueueHandler.initialLoad()

        pidgin.registerListener(ProxyMessages)
        pidgin.registerListener(ServerMessages)
        pidgin.registerListener(QueueMessages)
    }

}