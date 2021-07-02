package com.minexd.rift.bukkit.spoof.command

import com.google.gson.JsonArray
import com.minexd.rift.bukkit.spoof.SpoofHandler
import com.minexd.rift.bukkit.spoof.v1_8_R3.FakeEntityPlayer
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.entity.Player
import java.util.*

object SpoofAddCommand {

    @Command(
        names = ["rs add"],
        permission = "op"
    )
    @JvmStatic
    fun execute(player: Player) {
        val profile = SpoofHandler.nextRandomProfile()!!
        val uuid = UUID.fromString(profile[1].asString)

        if (!SpoofHandler.getFakePlayers().containsKey(uuid)) {
            val username = profile[0].asString
            val skin = Serializers.gson.fromJson<JsonArray>(profile[2].asString, JsonArray::class.java)[0].asJsonObject

            val gameProfile = GameProfile(uuid, username)
            gameProfile.properties.put("textures", Property("textures", skin.get("value").asString, skin.get("signature").asString))

            val fakePlayer = FakeEntityPlayer(gameProfile)
            SpoofHandler.addFakePlayer(fakePlayer)

            player.sendMessage("added fake player")
        }
    }

}