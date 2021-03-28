package com.minexd.rift.bukkit.spoof.v1_8_R3

import net.minecraft.server.v1_8_R3.PlayerInteractManager
import net.minecraft.server.v1_8_R3.World
import net.minecraft.server.v1_8_R3.WorldSettings

class FakePlayerInteractManager(world: World) : PlayerInteractManager(world) {

    override fun getGameMode(): WorldSettings.EnumGamemode {
        return WorldSettings.EnumGamemode.SURVIVAL
    }

}