package net.evilblock.rift.bukkit.util

import net.evilblock.cubed.util.NumberUtils
import org.bukkit.ChatColor

object Formats {

    @JvmStatic
    fun formatTps(tps: Double): String {
        return when {
            tps > 18.0 -> {
                "${ChatColor.GREEN}${NumberUtils.format(tps)}"
            }
            tps > 12.0 -> {
                "${ChatColor.YELLOW}${NumberUtils.format(tps)}"
            }
            tps > 8.0 -> {
                "${ChatColor.RED}${NumberUtils.format(tps)}"
            }
            else -> {
                "${ChatColor.DARK_RED}${NumberUtils.format(tps)}"
            }
        }
    }

}