package com.minexd.rift.bukkit.server.menu.button

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.time.TimeUtil
import com.minexd.rift.bukkit.util.Formats
import com.minexd.rift.server.Server
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

open class ServerButton(protected val server: Server) : Button() {

    override fun getName(player: Player): String {
        return if (!server.isOnline()) {
            "${ChatColor.RED}${ChatColor.BOLD}${server.displayName}"
        } else {
            if (server.whitelisted) {
                "${ChatColor.YELLOW}${ChatColor.BOLD}${server.displayName}"
            } else {
                "${ChatColor.GREEN}${ChatColor.BOLD}${server.displayName}"
            }
        }
    }

    override fun getDescription(player: Player): List<String> {
        return arrayListOf<String>().also { description ->

            description.add("${ChatColor.GRAY}(ID: ${server.id})")
            description.add("")
            description.add("${ChatColor.GRAY}Slots: ${ChatColor.YELLOW}${Numbers.format(server.slots)}")
            description.add("${ChatColor.GRAY}Whitelisted: ${if (server.whitelisted) "${ChatColor.GREEN}yes" else "${ChatColor.RED}no"}")
            description.add("${ChatColor.GRAY}Online Mode: ${if (server.onlineMode) "${ChatColor.GREEN}yes" else "${ChatColor.RED}no"}")
            description.add("${ChatColor.GRAY}Proxied: ${if (server.proxied) "${ChatColor.GREEN}yes" else "${ChatColor.RED}no"}")

            description.add("")

            if (server.lastHeartbeat != 0L) {
                description.add(buildString {
                    append("${ChatColor.GRAY}Last Heartbeat: ")

                    val timeAgo = ((System.currentTimeMillis() - server.lastHeartbeat) / 1000.0).toInt()
                    append("${ChatColor.YELLOW}${TimeUtil.formatIntoAbbreviatedString(timeAgo)}")
                })
            }

            if (server.isOnline()) {
                description.add("${ChatColor.GRAY}TPS: ${Formats.formatTps(server.currentTps)}")

                description.add(buildString {
                    append("${ChatColor.GRAY}Uptime: ")
                    append("${ChatColor.GREEN}${TimeUtil.formatIntoDetailedString((server.currentUptime / 1000.0).toInt())}")
                })

                description.add(buildString {
                    append("${ChatColor.GRAY}Players: ")
                    append("${ChatColor.GREEN}${Numbers.format(server.playerCount)}")
                    append("/${ChatColor.GREEN}${Numbers.format(server.slots)}")
                })
            }
        }
    }

    override fun getMaterial(player: Player): Material {
        return Material.WATER_LILY
    }

}