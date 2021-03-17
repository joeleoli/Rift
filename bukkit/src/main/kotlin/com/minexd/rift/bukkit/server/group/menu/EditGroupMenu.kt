package com.minexd.rift.bukkit.server.group.menu

import com.minexd.rift.server.ServerGroup
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

class EditGroupMenu(private val group: ServerGroup) : Menu() {

    init {
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Edit Group - ${group.displayName}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->

        }
    }

}