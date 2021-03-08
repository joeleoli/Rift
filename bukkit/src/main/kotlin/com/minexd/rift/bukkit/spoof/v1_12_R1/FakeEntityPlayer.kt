package com.minexd.rift.bukkit.spoof.v1_12_R1

import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_8_R3.EntityPlayer
import net.minecraft.server.v1_8_R3.MinecraftServer

class FakeEntityPlayer(gameProfile: GameProfile) : EntityPlayer(
    MinecraftServer.getServer(),
    MinecraftServer.getServer().getWorldServer(0),
    gameProfile,
    FakePlayerInteractManager(
        MinecraftServer.getServer().getWorldServer(0)
    )
) {

    private var ticked: Boolean = false

    init {
        playerConnection = FakePlayerConnection(this)
    }

    override fun t_() {
        if (!ticked) {
            super.t_()
            ticked = true
        }
    }

    // 1.12 override
//    override fun B_() {
//        if (!ticked) {
//            super.B_()
//            ticked = true
//        }
//    }

}