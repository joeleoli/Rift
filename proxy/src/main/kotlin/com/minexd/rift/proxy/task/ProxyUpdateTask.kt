package com.minexd.rift.proxy.task

import com.minexd.rift.proxy.ProxyHandler
import com.minexd.rift.proxy.RiftProxyPlugin

object ProxyUpdateTask : Runnable {

    override fun run() {
        val proxy = RiftProxyPlugin.instance.proxyInstance

        proxy.playerCount = RiftProxyPlugin.instance.proxy.onlineCount
        proxy.lastHeartbeat = System.currentTimeMillis()
        proxy.currentUptime = System.currentTimeMillis() - RiftProxyPlugin.enabledAt

        ProxyHandler.saveProxy(proxy)
    }

}