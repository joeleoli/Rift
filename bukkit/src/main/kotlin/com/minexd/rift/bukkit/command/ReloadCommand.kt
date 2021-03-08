package com.minexd.rift.bukkit.command

import net.evilblock.cubed.command.Command
import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object ReloadCommand {

    @Command(
        names = ["rift reload"],
        description = "Reloads the Rift configuration",
        permission = "op"
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        RiftBukkitPlugin.instance.reloadConfig()
        sender.sendMessage("${ChatColor.GREEN}Reloaded Rift configuration!")
    }

}