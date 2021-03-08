package com.minexd.rift.bukkit.server.command.parameter

import net.evilblock.cubed.command.data.parameter.ParameterType
import com.minexd.rift.bukkit.RiftBukkitPlugin
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ServerParameterType : ParameterType<Server> {

    override fun transform(sender: CommandSender, source: String): Server? {
        if (sender is Player && source.equals("self", ignoreCase = true)) {
            return RiftBukkitPlugin.instance.serverInstance
        }

        val server = ServerHandler.getServerById(source, ignoreCase = true)
        if (!server.isPresent) {
            sender.sendMessage("${ChatColor.RED}Couldn't find a server from the input: `${ChatColor.RESET}$source${ChatColor.RED}`")
        }

        return if (server.isPresent) {
            server.get()
        } else {
            null
        }
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        val completions = arrayListOf<String>()

        for (server in ServerHandler.getServers()) {
            if (server.id.startsWith(source, ignoreCase = true)) {
                completions.add(server.id)
            }
        }

        return completions
    }

}