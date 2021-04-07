package com.minexd.rift.bukkit.server.command

import com.minexd.rift.bukkit.server.group.menu.GroupsMenu
import com.minexd.rift.bukkit.util.Permissions
import net.evilblock.cubed.command.Command
import org.bukkit.entity.Player

object ServerGroupsCommand {

    @Command(
        names = ["rift server-groups"],
        description = "Opens a display of all server groups",
        permission = Permissions.SERVER_EDITOR
    )
    @JvmStatic
    fun execute(player: Player) {
        GroupsMenu().openMenu(player)
    }

}