package com.minexd.rift.bukkit.server.command

import com.minexd.rift.bukkit.server.group.menu.GroupsMenu
import com.minexd.rift.bukkit.util.Permissions
import net.evilblock.cubed.command.Command
import org.bukkit.entity.Player

object ServersCommand {

    @Command(
        names = ["rift servers"],
        description = "Opens a display of all servers",
        permission = Permissions.SERVER_EDITOR
    )
    @JvmStatic
    fun execute(player: Player) {
        GroupsMenu().openMenu(player)
    }

}