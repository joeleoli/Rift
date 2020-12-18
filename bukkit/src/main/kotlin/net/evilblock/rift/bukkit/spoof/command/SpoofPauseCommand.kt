package net.evilblock.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.rift.bukkit.spoof.SpoofHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object SpoofPauseCommand {

    @Command(
        names = ["rift gs pause"],
        description = "Pauses or un-pauses the GS system",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        SpoofHandler.togglePause()

        if (SpoofHandler.isPaused()) {
            sender.sendMessage("${ChatColor.YELLOW}GS is now paused!")
        } else {
            sender.sendMessage("${ChatColor.GREEN}GS is no longer paused!")
        }
    }

}