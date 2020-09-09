package net.evilblock.rift.bukkit.server.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.util.NumberUtils
import net.evilblock.rift.server.ServerHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object ServerDumpCommand {

    @Command(
        names = ["rift server dump"],
        description = "Prints server data to chat",
        permission = "op"
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GRAY}There are ${ChatColor.RED}${ChatColor.BOLD}${ServerHandler.getTotalPlayerCount()} ${ChatColor.GRAY}online players across ${ChatColor.RED}${ChatColor.BOLD}${ServerHandler.getOnlineServerCount()} ${ChatColor.GRAY}servers.")

        for (server in ServerHandler.getServers()) {
            val color = if (server.isOnline()) {
                if (server.whitelisted) {
                    ChatColor.YELLOW
                } else {
                    ChatColor.GREEN
                }
            } else {
                ChatColor.RED
            }

            sender.sendMessage("$color${server.displayName}:${server.port} ${ChatColor.RESET}- ${server.getPlayerCount().orElseGet { 0 }} players - ${NumberUtils.format(server.currentTps)} TPS")
        }
    }

}