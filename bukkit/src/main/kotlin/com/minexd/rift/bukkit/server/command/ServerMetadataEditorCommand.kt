package com.minexd.rift.bukkit.server.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.bukkit.server.menu.MetadataEditorMenu
import com.minexd.rift.bukkit.util.Permissions
import com.minexd.rift.server.Server
import org.bukkit.entity.Player

object ServerMetadataEditorCommand {

    @Command(
        names = ["rift server meta-editor"],
        description = "Opens the Server Metadata Editor",
        permission = Permissions.SERVER_EDITOR
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "server") server: Server) {
        MetadataEditorMenu(server).openMenu(player)
    }

}