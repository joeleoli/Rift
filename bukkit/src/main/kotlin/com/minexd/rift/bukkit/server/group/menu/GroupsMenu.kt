package com.minexd.rift.bukkit.server.group.menu

import com.minexd.rift.bukkit.server.group.menu.button.GroupButton
import com.minexd.rift.bukkit.server.menu.ServersMenu
import com.minexd.rift.server.ServerGroup
import com.minexd.rift.server.ServerHandler
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class GroupsMenu : PaginatedMenu() {

    init {
        updateAfterClick = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Server Groups"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = AddServerGroupButton()

            for (group in ServerHandler.getGroups()) {
                buttons[buttons.size] = EditGroupButton(group)
            }
        }
    }

    private inner class EditGroupButton(group: ServerGroup) : GroupButton(group) {
        override fun getDescription(player: Player): List<String> {
            return (super.getDescription(player) as MutableList<String>).also { desc ->
                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit group"))
                desc.add(styleAction(ChatColor.AQUA, "RIGHT-CLICK", "to view servers"))
                desc.add(styleAction(ChatColor.RED, "SHIFT-CLICK", "to delete group"))
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isShiftClick) {
                ConfirmMenu { confirmed ->
                    if (confirmed) {
                        if (group.servers.isNotEmpty()) {
                            player.sendMessage("${ChatColor.RED}You can only delete a group if it has no servers attached!")
                            return@ConfirmMenu
                        }

                        ServerHandler.deleteGroup(group)
                    }
                }.openMenu(player)

                return
            }

            if (clickType.isLeftClick) {
                EditGroupMenu(group).openMenu(player)
            } else if (clickType.isRightClick) {
                ServersMenu(group).openMenu(player)
            }
        }
    }

    private inner class AddServerGroupButton : AddButton() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Create New Group"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also { desc ->
                desc.add("")
                desc.addAll(TextSplitter.split(text = "Create a new group by completing the setup procedure."))
                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to create new group"))
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                InputPrompt().withText("${ChatColor.GREEN}Please enter a name for the group.")
                    .withRegex(EzPrompt.IDENTIFIER_REGEX)
                    .acceptInput { input ->
                        if (ServerHandler.getGroupById(input) != null) {
                            player.sendMessage("${ChatColor.RED}That ID is taken by an already existing group!")
                            return@acceptInput
                        }

                        val group = ServerGroup(input)
                        ServerHandler.groups[group.id.toLowerCase()] = group

                        Tasks.async {
                            ServerHandler.saveGroup(group)
                        }

                        EditGroupMenu(group).openMenu(player)
                    }
                    .onFail { this@GroupsMenu.openMenu(player) }
                    .start(player)
            }
        }
    }

}