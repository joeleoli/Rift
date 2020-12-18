package net.evilblock.rift.bukkit.spoof.v1_12_R1

import net.minecraft.server.v1_12_R1.MinecraftServer
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection

class FakePlayerConnection(player: FakeEntityPlayer) : PlayerConnection(MinecraftServer.getServer(), fakeNetworkManager, player) {

    companion object {
        private val fakeNetworkManager = FakeNetworkManager()
    }

    override fun e() {
        super.e()
    }

    override fun sendPacket(packet: Packet<*>?) {

    }


}