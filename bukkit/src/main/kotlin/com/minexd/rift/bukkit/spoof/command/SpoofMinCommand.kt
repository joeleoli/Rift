package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.SpoofHandler
import org.bukkit.command.CommandSender

object SpoofMinCommand {

    @Command(
        names = ["rift secret min"],
        permission = "op",
        description = "Sets GS min"
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "min") min: Int) {
        RiftBukkitPlugin.instance.setSpoofMin(min)
        sender.sendMessage("min: ${RiftBukkitPlugin.instance.readSpoofMin()}")
    }

}