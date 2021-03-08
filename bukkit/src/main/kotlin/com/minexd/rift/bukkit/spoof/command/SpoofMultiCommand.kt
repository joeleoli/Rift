package com.minexd.rift.bukkit.spoof.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.bukkit.spoof.SpoofHandler
import org.bukkit.command.CommandSender

object SpoofMultiCommand {

    @Command(
        names = ["rift gs mx"],
        permission = "op",
        description = "Sets GS multi"
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "multi") multi: Double) {
        RiftBukkitPlugin.instance.setSpoofMultiplier(multi)
        sender.sendMessage("multi: ${RiftBukkitPlugin.instance.readSpoofMultiplier()}")
    }

}