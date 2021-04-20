package com.minexd.rift.bukkit.jump

import com.minexd.rift.Rift
import com.minexd.rift.bukkit.util.Permissions
import com.minexd.rift.server.Server
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.pidgin.message.Message
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object SendAllFromToCommand {

    @Command(
        names = ["send-all"],
        description = "Sends all players from one server to another",
        permission = Permissions.JUMP_ALL,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["e"], defaultValue = false, description = "Exclude staff members") exclude: Boolean,
        @Param(name = "from") from: Server,
        @Param(name = "to") to: Server
    ) {
        Rift.instance.proxyChannel.sendMessage(Message(
            id = "SendAllFromTo",
            data = mapOf(
                "From" to from.id,
                "To" to to.id,
                "Exclude" to exclude
            )
        ))
        sender.sendMessage("${ChatColor.GREEN}Command sent!")
    }

}