package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.util.time.TimeUtil
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.SpoofHandler
import com.minexd.rift.bukkit.spoof.thread.SpoofThread
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import kotlin.math.max

object SpoofStatusCommand {

    @Command(
        names = ["rift status"],
        description = "Show the status of Rift",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        val totalCount = Bukkit.getOnlinePlayers().size
        val fakeCount = SpoofHandler.getFakePlayers().size
        val realCount = totalCount - fakeCount
        val targetCount = max(RiftBukkitPlugin.instance.readSpoofMin(), (realCount * RiftBukkitPlugin.instance.readSpoofMultiplier()).toInt())
        val nextChange = TimeUtil.formatIntoAbbreviatedString(((SpoofThread.nextChange - System.currentTimeMillis()) / 1000.0).toInt())

        sender.sendMessage("")

        sender.sendMessage(buildString {
            append(" ${ChatColor.YELLOW}${ChatColor.BOLD}Rift Status")

            if (SpoofHandler.isEnabled()) {
                append(" ${ChatColor.GRAY}(${ChatColor.GREEN}enabled${ChatColor.GRAY})")
            } else {
                append(" ${ChatColor.GRAY}(${ChatColor.RED}disabled${ChatColor.GRAY})")
            }

            if (SpoofHandler.isPaused()) {
                append(" ${ChatColor.GRAY}(${ChatColor.YELLOW}paused${ChatColor.GRAY})")
            }
        })

        sender.sendMessage("")

        sender.sendMessage(buildString {
            append(" ${ChatColor.GREEN}${ChatColor.BOLD}REAL${ChatColor.GRAY}: $realCount")
            append(" ${ChatColor.DARK_GRAY}/ ")
            append("${ChatColor.YELLOW}${ChatColor.BOLD}FAKE${ChatColor.GRAY}: $fakeCount")
            append(" ${ChatColor.DARK_GRAY}/ ")
            append("${ChatColor.AQUA}${ChatColor.BOLD}TOTAL${ChatColor.GRAY}: $totalCount")
            append(" ${ChatColor.DARK_GRAY}/ ")
            append("${ChatColor.RED}${ChatColor.BOLD}TARGET${ChatColor.GRAY}: $targetCount")
        })

        sender.sendMessage("")
        sender.sendMessage(" ${ChatColor.YELLOW}min-max: ${ChatColor.WHITE}${RiftBukkitPlugin.instance.readSpoofMin()} -> ${RiftBukkitPlugin.instance.readSpoofMax()}")
        sender.sendMessage(" ${ChatColor.YELLOW}delay: ${ChatColor.WHITE}${RiftBukkitPlugin.instance.readSpoofMinDelay()} -> ${RiftBukkitPlugin.instance.readSpoofMaxDelay()}")
        sender.sendMessage(" ${ChatColor.YELLOW}multiplier: ${ChatColor.WHITE}${RiftBukkitPlugin.instance.readSpoofMultiplier()}")
        sender.sendMessage(" ${ChatColor.YELLOW}next change: ${ChatColor.WHITE}$nextChange")
        sender.sendMessage("")
    }

}