package com.minexd.rift.bukkit.queue.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import com.minexd.rift.bukkit.server.menu.SelectServerMenu
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueHandler
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class QueueEditorMenu : Menu() {

    init {
        autoUpdate = true
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Queue Editor"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        buttons[0] = AddQueueButton()
        buttons[2] = EditPriorityButton()

        for (i in 9..17) {
            buttons[i] = GlassButton(0)
        }

        for ((i, queue) in QueueHandler.getQueues().withIndex()) {
            buttons[i + 18] = QueueButton(queue)
        }

        return buttons
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 54
    }

    private inner class AddQueueButton : AddButton() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Create New Queue"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "Create a new queue by completing the setup procedure.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to create a new queue")

            return description
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                EzPrompt.Builder()
                    .regex(EzPrompt.IDENTIFIER_REGEX)
                    .charLimit(16)
                    .promptText("${ChatColor.GREEN}Please input an ID for the queue.")
                    .acceptInput { input ->
                        if (QueueHandler.getQueueById(input) != null) {
                            player.sendMessage("${ChatColor.RED}A queue with an ID of `$input` already exists.")
                            return@acceptInput
                        }

                        SelectServerMenu { route ->
                            if (route == null) {
                                this@QueueEditorMenu.openMenu(player)
                                return@SelectServerMenu
                            }

                            val queue = Queue(input, route)
                            QueueHandler.trackQueue(queue)

                            Tasks.async {
                                QueueHandler.saveQueue(queue)
                            }

                            player.sendMessage("${ChatColor.GREEN}Successfully created the `${queue.id}` queue!")

                            Tasks.delayed(1L) {
                                EditQueueMenu(queue)
                                    .openMenu(player)
                            }
                        }.openMenu(player)
                    }
                    .build()
                    .start(player)
            }
        }
    }

    private inner class EditPriorityButton : TexturedHeadButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFhOTQ2M2ZkM2M0MzNkNWUxZDlmZWM2ZDVkNGIwOWE4M2E5NzBiMGI3NGRkNTQ2Y2U2N2E3MzM0OGNhYWIifX19") {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Priority"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(
                TextSplitter.split(
                    length = 40,
                    text = "Control the queue priority by managing a map of permission to priority. The higher the priority, the faster players with that permission get into the server.",
                    linePrefix = ChatColor.GRAY.toString()
                ))

            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to edit priority")

            return description
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            EditPriorityMenu().openMenu(player)
        }
    }

    private inner class QueueButton(private val queue: Queue) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.YELLOW}${ChatColor.BOLD}${queue.id}"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("${ChatColor.GRAY}(${queue.cachedEntries.size} players in queue)")
            description.add("")
            description.add("${ChatColor.GRAY}Route: ${ChatColor.YELLOW}${queue.route.displayName}")
            description.add("${ChatColor.GRAY}Open: ${if (queue.open) "${ChatColor.GREEN}yes" else "${ChatColor.RED}no"}")
            description.add("${ChatColor.GRAY}Polling: ${if (queue.polling) "${ChatColor.GREEN}yes" else "${ChatColor.RED}no"}")
            description.add("${ChatColor.GRAY}Polling Rate: ${ChatColor.YELLOW}${queue.pollingRate}")
            description.add("${ChatColor.GRAY}Polling Size: ${ChatColor.YELLOW}${queue.pollingSize}")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to edit queue")
            description.add("${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK ${ChatColor.RED}to delete queue")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.COMPASS
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                EditQueueMenu(queue).openMenu(player)
            } else if (clickType.isRightClick) {
                ConfirmMenu { confirmed ->
                    if (confirmed) {
                        QueueHandler.deleteQueue(queue)
                        player.sendMessage("${ChatColor.GREEN}Successfully deleted the `${queue.id}` queue.")
                    } else {
                        player.sendMessage("${ChatColor.YELLOW}No changes made.")
                    }

                    QueueEditorMenu().openMenu(player)
                }.openMenu(player)
            }
        }
    }

}