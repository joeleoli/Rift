package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.command.CommandSender

object SpoofMaxDelayCommand {

    @Command(
        names = ["rs max-delay"],
        permission = "op",
        description = "Sets GS max-delay"
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "max-delay") maxDelay: Long) {
        RiftBukkitPlugin.instance.setSpoofMaxDelay(maxDelay)
        sender.sendMessage("max-delay: ${RiftBukkitPlugin.instance.readSpoofMaxDelay()}")
    }

}