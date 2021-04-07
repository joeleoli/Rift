package com.minexd.rift.bukkit.util

import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.ChatColor

object Formats {

    @JvmStatic
    fun formatTps(tps: Double): String {
        return when {
            tps > 18.0 -> {
                "${ChatColor.GREEN}${TextUtil.formatTPS(tps)}"
            }
            tps > 12.0 -> {
                "${ChatColor.YELLOW}${TextUtil.formatTPS(tps)}"
            }
            tps > 8.0 -> {
                "${ChatColor.RED}${TextUtil.formatTPS(tps)}"
            }
            else -> {
                "${ChatColor.DARK_RED}${TextUtil.formatTPS(tps)}"
            }
        }
    }

}