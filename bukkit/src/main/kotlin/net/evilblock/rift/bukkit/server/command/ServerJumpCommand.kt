package net.evilblock.rift.bukkit.server.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.bungee.BungeeUtil
import net.evilblock.rift.bukkit.util.Permissions
import net.evilblock.rift.server.Server
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object ServerJumpCommand {

    @Command(
        names = ["rift server jump", "rift server join", "sj"],
        description = "Jump to a server",
        permission = Permissions.SERVER_EDITOR
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "server") server: Server) {
        player.sendMessage("${ChatColor.GREEN}Attempting to send you to ${ChatColor.WHITE}${server.id}${ChatColor.GREEN}...")
        BungeeUtil.sendToServer(player, server.id)
    }

}