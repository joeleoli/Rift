package com.minexd.rift.bukkit.jump

import com.google.gson.JsonArray
import com.minexd.rift.Rift
import com.minexd.rift.bukkit.util.Permissions
import com.minexd.rift.server.Server
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.pidgin.message.Message
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object SendAllToCommand {

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
        @Param(name = "to") to: Server
    ) {
        val includedPlayers = JsonArray()

        for (player in Bukkit.getOnlinePlayers()) {
            if (exclude && player.hasPermission(Permissions.JUMP_ALL_EXCLUSION)) {
                continue
            }

            includedPlayers.add(player.uniqueId.toString())

            player.sendMessage("${ChatColor.GREEN}You've been sent to ${ChatColor.DARK_AQUA}${ChatColor.BOLD}${to.displayName}${ChatColor.GREEN}!")
        }

        Rift.instance.proxyChannel.sendMessage(Message(
            id = "SendToLobby",
            data = mapOf(
                "Players" to includedPlayers
            )
        ))

        sender.sendMessage("${ChatColor.GREEN}Sent ${ChatColor.DARK_AQUA}${includedPlayers.size()} ${ChatColor.GREEN}players to ${ChatColor.DARK_AQUA}${ChatColor.BOLD}${to.displayName}${ChatColor.GREEN}!")
    }

}