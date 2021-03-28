package com.minexd.rift.bukkit.spoof.v1_8_R3

import net.minecraft.server.v1_8_R3.EnumProtocolDirection
import net.minecraft.server.v1_8_R3.NetworkManager

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress


class FakeNetworkManager : NetworkManager(EnumProtocolDirection.SERVERBOUND) {

    init {
        channel = EmptyChannel(null)
    }

    override fun getSocketAddress(): SocketAddress? {
        return try {
            InetSocketAddress(InetAddress.getLocalHost(), 0)
        } catch (e: Throwable) {
            null
        }
    }

    override fun g(): Boolean {
        return true
    }

    // 1.12 override
//    override fun isConnected(): Boolean {
//        return true
//    }

}