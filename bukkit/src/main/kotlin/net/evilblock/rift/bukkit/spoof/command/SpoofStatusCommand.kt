package net.evilblock.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.util.TimeUtil
import net.evilblock.rift.bukkit.RiftBukkitPlugin
import net.evilblock.rift.bukkit.spoof.SpoofHandler
import net.evilblock.rift.bukkit.spoof.thread.SpoofThread
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import kotlin.math.max

object SpoofStatusCommand {

    @Command(
        names = ["rift gs status"],
        description = "Show the status of the GS system",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        val totalCount = Bukkit.getOnlinePlayers().size
        val fakeCount = SpoofHandler.getFakePlayers().size
        val realCount = totalCount - fakeCount
        val targetCount = max(RiftBukkitPlugin.instance.readSpoofMin(), (realCount * RiftBukkitPlugin.instance.readSpoofMultiplier()).toInt())

        sender.sendMessage("")
        sender.sendMessage(" ${ChatColor.YELLOW}${ChatColor.BOLD}GS STATUS (KEK)")
        sender.sendMessage(" ${ChatColor.GREEN}${ChatColor.BOLD}${ChatColor.BLUE}${ChatColor.BOLD}T: ${ChatColor.GRAY}${totalCount} ${ChatColor.DARK_GRAY}${ChatColor.BOLD}// ${ChatColor.GREEN}${ChatColor.BOLD}R: ${ChatColor.GRAY}${realCount} ${ChatColor.DARK_GRAY}${ChatColor.BOLD}// ${ChatColor.YELLOW}${ChatColor.BOLD}F: ${ChatColor.GRAY}${fakeCount}")
        sender.sendMessage(" ${ChatColor.DARK_RED}${ChatColor.BOLD}Target: ${ChatColor.GRAY}$targetCount")
        sender.sendMessage("")
        sender.sendMessage(" min: ${RiftBukkitPlugin.instance.readSpoofMin()}, max: ${RiftBukkitPlugin.instance.readSpoofMax()}, multi: ${RiftBukkitPlugin.instance.readSpoofMultiplier()}")
        sender.sendMessage(" next change: ${TimeUtil.formatIntoAbbreviatedString(((SpoofThread.nextChange - System.currentTimeMillis()) / 1000.0).toInt())}")
        sender.sendMessage("")
    }

}