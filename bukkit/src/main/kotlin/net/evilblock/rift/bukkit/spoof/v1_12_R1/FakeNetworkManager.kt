package net.evilblock.rift.bukkit.spoof.v1_12_R1

import net.minecraft.server.v1_12_R1.EnumProtocolDirection
import net.minecraft.server.v1_12_R1.NetworkManager
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

    override fun isConnected(): Boolean {
        return true
    }

}