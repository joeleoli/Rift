package com.minexd.rift.bukkit.spoof.thread

import com.google.gson.JsonArray
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.SpoofHandler
import com.minexd.rift.bukkit.spoof.v1_8_R3.FakeEntityPlayer
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

object SpoofThread : Thread("Rift-Spoof") {

    var nextChange: Long = System.currentTimeMillis()

    override fun run() {
        primary@while (true) {
            if (System.currentTimeMillis() < nextChange) {
                sleep()
                continue
            }

            try {
                if (!SpoofHandler.isEnabled()) {
                    sleep()
                    continue
                }

                if (SpoofHandler.isPaused()) {
                    sleep()
                    continue
                }

                if (Bukkit.hasWhitelist()) {
                    sleep()
                    continue
                }

                when (determineAction()) {
                    SpoofAction.ADD_PLAYER -> {
                        val profile = SpoofHandler.nextRandomProfile() ?: continue@primary
                        val uuid = UUID.fromString(profile[1].asString)

                        if (!SpoofHandler.getFakePlayers().containsKey(uuid)) {
                            val username = profile[0].asString
                            val skin = Serializers.gson.fromJson<JsonArray>(profile[2].asString, JsonArray::class.java)[0].asJsonObject

                            val gameProfile = GameProfile(uuid, username)
                            gameProfile.properties.put("textures", Property("textures", skin.get("value").asString, skin.get("signature").asString))

                            SpoofHandler.addFakePlayer(FakeEntityPlayer(gameProfile))

                            delay()
                        }
                    }
                    SpoofAction.REMOVE_PLAYER -> {
                        if (SpoofHandler.getFakePlayers().isNotEmpty()) {
                            val fakePlayer = SpoofHandler.getFakePlayers().values.first()
                            SpoofHandler.removeFakePlayer(fakePlayer)

                            delay()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            sleep()
        }
    }

    private fun sleep() {
        sleep(RiftBukkitPlugin.instance.readSpoofInterval() * 50L)
    }

    private fun delay() {
        nextChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(
            RiftBukkitPlugin.instance.readSpoofMinDelay(),
            RiftBukkitPlugin.instance.readSpoofMaxDelay()
        )
    }

    private fun determineAction(): SpoofAction? {
        val totalCount = Bukkit.getOnlinePlayers().size
        val fakeCount = SpoofHandler.getFakePlayerCount()

        if (totalCount < RiftBukkitPlugin.instance.readSpoofMin()) {
            return SpoofAction.ADD_PLAYER
        }

        if (totalCount + RiftBukkitPlugin.instance.readSpoofBuffer() > Bukkit.getMaxPlayers() || fakeCount > RiftBukkitPlugin.instance.readSpoofMax()) {
            return SpoofAction.REMOVE_PLAYER
        }

        val realCount = totalCount - fakeCount
        val targetCount = max(RiftBukkitPlugin.instance.readSpoofMin(), (realCount * RiftBukkitPlugin.instance.readSpoofMultiplier()).toInt())
        return when {
            totalCount < targetCount -> {
                SpoofAction.ADD_PLAYER
            }
            totalCount > targetCount -> {
                SpoofAction.REMOVE_PLAYER
            }
            else -> null
        }
    }

    enum class SpoofAction {
        ADD_PLAYER,
        REMOVE_PLAYER
    }

}