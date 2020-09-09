package net.evilblock.rift.bukkit.queue.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.buttons.NumberButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.TextSplitter
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.rift.queue.QueueHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class EditPriorityMenu : PaginatedMenu() {

    init {
        autoUpdate = true
        updateAfterClick = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Edit Priority"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>? {
        return mapOf(2 to AddPriorityButton())
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for ((permission, priority) in QueueHandler.getPriority().entries.sortedBy { it.value }.reversed()) {
            buttons[buttons.size] = PermissionButton(permission, priority)
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 45
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 54
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Tasks.delayed(1L) {
                QueueEditorMenu().openMenu(player)
            }
        }
    }

    private inner class AddPriorityButton : AddButton() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Create New Priority"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(
                TextSplitter.split(
                length = 40,
                text = "Create a new priority entry by completing the setup procedure.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to setup a new permission")

            return description
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                EzPrompt.Builder()
                    .regex(PERMISSION_REGEX)
                    .promptText("${ChatColor.GREEN}Please input a permission node.")
                    .acceptInput { _, input ->
                        Tasks.async {
                            QueueHandler.savePriority(input, 0)

                            Tasks.sync {
                                this@EditPriorityMenu.openMenu(player)
                            }
                        }
                    }
                    .build()
                    .start(player)
            }
        }
    }

    private inner class PermissionButton(private val permission: String, priority: Int) : NumberButton(priority) {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}$permission ${ChatColor.GRAY}($number)"
        }

        override fun getDescription(player: Player): List<String> {
            return super.getDescription(player).toMutableList().also {
                it.add("")
                it.add("${ChatColor.RED}${ChatColor.BOLD}MIDDLE-CLICK ${ChatColor.RED}to delete priority")
            }
        }

        override fun onChange(number: Int) {
            Tasks.async {
                QueueHandler.savePriority(permission, number)
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType == ClickType.MIDDLE) {
                ConfirmMenu { confirmed ->
                    if (confirmed) {
                        Tasks.async {
                            QueueHandler.deletePriority(permission)
                        }
                    } else {
                        player.sendMessage("${ChatColor.YELLOW}No changes made.")
                    }

                    this@EditPriorityMenu.openMenu(player)
                }.openMenu(player)
            } else {
                super.clicked(player, slot, clickType, view)
            }
        }
    }

    companion object {
        private val PERMISSION_REGEX = "[a-zA-Z0-9_.-]*".toRegex()
    }

}