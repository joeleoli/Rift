package net.evilblock.rift.bukkit.server.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.rift.server.Server
import net.evilblock.rift.server.ServerHandler
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class SelectRouteMenu(private val onSelect: (Server) -> Unit) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Select Route"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (server in ServerHandler.getServers()) {
            buttons[buttons.size] = SelectServerButton(server)
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 45
    }

    private inner class SelectServerButton(server: Server) : ServerButton(server) {
        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                player.closeInventory()
                onSelect.invoke(server)
            }
        }
    }

}