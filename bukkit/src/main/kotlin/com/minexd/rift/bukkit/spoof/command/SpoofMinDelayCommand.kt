package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.command.CommandSender

object SpoofMinDelayCommand {

    @Command(
        names = ["rs min-delay"],
        permission = "op",
        description = "Sets GS min-delay"
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "min-delay") minDelay: Long) {
        RiftBukkitPlugin.instance.setSpoofMinDelay(minDelay)
        sender.sendMessage("min-delay: ${RiftBukkitPlugin.instance.readSpoofMinDelay()}")
    }

}