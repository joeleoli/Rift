package com.minexd.rift.bukkit.spoof.v1_12_R1

import net.minecraft.server.v1_8_R3.MinecraftServer
import net.minecraft.server.v1_8_R3.Packet
import net.minecraft.server.v1_8_R3.PlayerConnection

class FakePlayerConnection(player: FakeEntityPlayer) : PlayerConnection(MinecraftServer.getServer(),
    fakeNetworkManager, player) {

    companion object {
        private val fakeNetworkManager = FakeNetworkManager()
    }

    override fun c() {
        super.c()
    }

    // 1.12 override
//    override fun e() {
//        super.e()
//    }

    override fun sendPacket(packet: Packet<*>?) {

    }


}