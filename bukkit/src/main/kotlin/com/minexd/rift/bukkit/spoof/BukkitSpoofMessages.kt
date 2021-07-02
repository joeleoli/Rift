package com.minexd.rift.bukkit.spoof

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.minexd.rift.Rift
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.v1_8_R3.FakeEntityPlayer
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.pidgin.message.Message
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import java.util.*

object BukkitSpoofMessages : MessageListener {

    @IncomingMessageHandler(SpoofHandler.ADD_SPOOFED_PLAYER)
    fun onAddSpoofedPlayer(data: JsonObject) {
        println("Received ADD_SPOOFED_PLAYER for ${data["Player"].asString} (${RiftBukkitPlugin.instance.readServerId()}->${data["Route"].asString})")

        if (RiftBukkitPlugin.instance.readServerId() == data["Route"].asString) {
            val player = UUID.fromString(data["Player"].asString)

            if (!SpoofHandler.areFakeProfilesLoaded()) {
                println("profiles not loaded")
                return
            }

            val profile = SpoofHandler.getFakeProfile(player)!!
            val username = profile[0].asString
            val skin = Serializers.gson.fromJson<JsonArray>(profile[2].asString, JsonArray::class.java)[0].asJsonObject

            val gameProfile = GameProfile(player, username)
            gameProfile.properties.put("textures", Property("textures", skin.get("value").asString, skin.get("signature").asString))

            println("added fake player $username")
            SpoofHandler.addFakePlayer(FakeEntityPlayer(gameProfile))
        }
    }

    @IncomingMessageHandler(SpoofHandler.REMOVE_SPOOFED_PLAYER)
    fun onRemoveSpoofedPlayer(data: JsonObject) {
        println("Received REMOVE_SPOOFED_PLAYER for ${data["Player"].asString}")

        val player = Bukkit.getPlayer(UUID.fromString(data["Player"].asString)) ?: return

        if (!SpoofHandler.isFakePlayer(player)) {
            return
        }

        SpoofHandler.removeFakePlayer((player as CraftPlayer).handle as FakeEntityPlayer)
    }

    @IncomingMessageHandler(SpoofHandler.SWITCH_SPOOFED_PLAYER)
    fun onSwitchSpoofedPlayer(data: JsonObject) {
        println("Received SWITCH_SPOOFED_PLAYER for ${data["Player"].asString}")

        val player = Bukkit.getPlayer(UUID.fromString(data["Player"].asString)) ?: return

        if (!SpoofHandler.isFakePlayer(player)) {
            return
        }

        SpoofHandler.removeFakePlayer((player as CraftPlayer).handle as FakeEntityPlayer)

        Rift.instance.mainChannel.sendMessage(Message(SpoofHandler.ADD_SPOOFED_PLAYER, mapOf(
            "Player" to data["Player"].asString,
            "Route" to data["Route"].asString
        )))
    }

}