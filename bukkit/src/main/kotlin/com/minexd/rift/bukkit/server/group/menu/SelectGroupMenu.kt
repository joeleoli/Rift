package com.minexd.rift.bukkit.server.group.menu

import com.minexd.rift.bukkit.server.group.menu.button.GroupButton
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import com.minexd.rift.server.ServerGroup
import com.minexd.rift.server.ServerHandler
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class SelectGroupMenu(private val onSelect: (ServerGroup?) -> Unit) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Select Group"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (group in ServerHandler.getGroups()) {
            buttons[buttons.size] = SelectGroupButton(group)
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 45
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            onSelect.invoke(null)
        }
    }

    private inner class SelectGroupButton(group: ServerGroup) : GroupButton(group) {
        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                player.closeInventory()
                onSelect.invoke(group)
            }
        }
    }

}