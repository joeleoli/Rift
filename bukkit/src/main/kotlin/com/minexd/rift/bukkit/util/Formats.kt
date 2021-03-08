package com.minexd.rift.bukkit.util

import net.evilblock.cubed.util.math.Numbers
import org.bukkit.ChatColor

object Formats {

    @JvmStatic
    fun formatTps(tps: Double): String {
        return when {
            tps > 18.0 -> {
                "${ChatColor.GREEN}${Numbers.format(tps)}"
            }
            tps > 12.0 -> {
                "${ChatColor.YELLOW}${Numbers.format(tps)}"
            }
            tps > 8.0 -> {
                "${ChatColor.RED}${Numbers.format(tps)}"
            }
            else -> {
                "${ChatColor.DARK_RED}${Numbers.format(tps)}"
            }
        }
    }

}