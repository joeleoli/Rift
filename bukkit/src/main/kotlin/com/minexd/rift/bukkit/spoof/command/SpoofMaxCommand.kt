package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.command.CommandSender

object SpoofMaxCommand {

    @Command(
        names = ["rs max"],
        permission = "op",
        description = "Sets GS max"
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "max") max: Int) {
        RiftBukkitPlugin.instance.setSpoofMax(max)
        sender.sendMessage("max: ${RiftBukkitPlugin.instance.readSpoofMax()}")
    }

}