package net.evilblock.rift.bukkit.spoof.v1_12_R1

import net.minecraft.server.v1_12_R1.EnumGamemode
import net.minecraft.server.v1_12_R1.PlayerInteractManager
import net.minecraft.server.v1_12_R1.World

class FakePlayerInteractManager(world: World) : PlayerInteractManager(world) {

    override fun getGameMode(): EnumGamemode {
        return EnumGamemode.SURVIVAL
    }

}