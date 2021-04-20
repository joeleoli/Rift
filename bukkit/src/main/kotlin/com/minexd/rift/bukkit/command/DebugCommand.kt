package com.minexd.rift.bukkit.command

import com.minexd.rift.Rift
import net.evilblock.cubed.command.Command
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object DebugCommand {

    @Command(
        names = ["rift debug"],
        description = "Toggles Rift debug messages",
        permission = "op"
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        Rift.instance.mainChannel.debug = !Rift.instance.mainChannel.debug

        if (Rift.instance.mainChannel.debug) {
            sender.sendMessage("${ChatColor.GREEN}Debug enabled!")
        } else {
            sender.sendMessage("${ChatColor.RED}Debug disabled!")
        }
    }

}