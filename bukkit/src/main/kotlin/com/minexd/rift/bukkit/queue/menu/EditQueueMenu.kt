package com.minexd.rift.bukkit.queue.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.rift.bukkit.server.menu.SelectRouteMenu
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueHandler
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import kotlin.math.round

class EditQueueMenu(private val queue: Queue) : Menu() {

    init {
        autoUpdate = true
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Edit Queue - ${queue.id}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        buttons[0] = EditRouteButton()
        buttons[1] = ToggleOpenButton()
        buttons[2] = TogglePollingButton()
        buttons[3] = EditPollingRateButton()
        buttons[4] = EditPollingSizeButton()
        buttons[5] = FlushButton()

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Tasks.delayed(1L) {
                QueueEditorMenu().openMenu(player)
            }
        }
    }

    private inner class EditRouteButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Route"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "Change the server the player gets routed to when polled by the queue.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to edit route")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.ENDER_PEARL
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                SelectRouteMenu { route ->
                    if (!route.proxied) {
                        player.sendMessage("${ChatColor.RED}You can't route a queue to a server that is not proxied.")
                        return@SelectRouteMenu
                    }

                    queue.route = route

                    Tasks.async {
                        QueueHandler.saveQueue(queue)
                    }

                    Tasks.delayed(1L) {
                        this@EditQueueMenu.openMenu(player)
                    }
                }.openMenu(player)
            }
        }
    }

    private inner class ToggleOpenButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Toggle Open"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "If players can join the queue, regardless of the route server state, or if this queue is polling.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")

            if (queue.open) {
                description.add("${ChatColor.GREEN}${ChatColor.BOLD}This queue is open")
            } else {
                description.add("${ChatColor.RED}${ChatColor.BOLD}This queue is closed")
            }

            description.add("")

            if (queue.open) {
                description.add("${ChatColor.RED}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.RED}to close queue")
            } else {
                description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to open queue")
            }

            return description
        }

        override fun getMaterial(player: Player): Material {
            return if (queue.open) {
                Material.STORAGE_MINECART
            } else {
                Material.MINECART
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                queue.open = !queue.open

                Tasks.async {
                    QueueHandler.saveQueue(queue)
                }
            }
        }
    }

    private inner class TogglePollingButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Toggle Polling"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "If the queue is polling, meaning sending batches of players to the route server.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")

            if (queue.polling) {
                description.add("${ChatColor.GREEN}${ChatColor.BOLD}This queue is polling")
            } else {
                description.add("${ChatColor.RED}${ChatColor.BOLD}This queue is not polling")
            }

            description.add("")

            if (queue.polling) {
                description.add("${ChatColor.RED}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.RED}to stop polling")
            } else {
                description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to start polling")
            }

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.REDSTONE_TORCH_ON
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                queue.polling = !queue.polling

                Tasks.async {
                    QueueHandler.saveQueue(queue)
                }
            }
        }
    }

    private inner class EditPollingRateButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Polling Rate"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "The polling rate is the interval between sending batches of players. The rate is measured in seconds.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.GRAY}Current polling rate: ${Numbers.format(queue.pollingRate)}")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to increase rate by +1")
            description.add("${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK ${ChatColor.RED}to decrease rate by -1")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}SHIFT LEFT-CLICK ${ChatColor.GREEN}to increase rate by +0.1")
            description.add("${ChatColor.RED}${ChatColor.BOLD}SHIFT RIGHT-CLICK ${ChatColor.RED}to decrease rate by -0.1")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.DIODE
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val original = queue.pollingRate

            val mod = if (clickType.isShiftClick) {
                0.1
            } else {
                1.0
            }

            if (clickType.isLeftClick) {
                queue.pollingRate = round(((queue.pollingRate + mod) * 10.0) / 10.0)
            } else if (clickType.isRightClick) {
                queue.pollingRate = round((0.0.coerceAtLeast(queue.pollingRate - mod) * 10.0) / 10.0)
            }

            if (queue.pollingRate != original) {
                Tasks.async {
                    QueueHandler.saveQueue(queue)
                }
            }
        }
    }

    private inner class EditPollingSizeButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Polling Size"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "The polling size is the amount of players in a batch that can be polled.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.GRAY}Current polling size: ${Numbers.format(queue.pollingSize)}")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to increase size by +1")
            description.add("${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK ${ChatColor.RED}to decrease size by -1")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}SHIFT LEFT-CLICK ${ChatColor.GREEN}to increase size by +10")
            description.add("${ChatColor.RED}${ChatColor.BOLD}SHIFT RIGHT-CLICK ${ChatColor.RED}to decrease size by -10")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.REDSTONE_COMPARATOR
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val original = queue.pollingSize
            val mod = if (clickType.isShiftClick) 10 else 1
            if (clickType.isLeftClick) {
                queue.pollingSize += mod
            } else if (clickType.isRightClick) {
                queue.pollingSize = 0.coerceAtLeast(queue.pollingSize - mod)
            }

            if (queue.pollingSize != original) {
                Tasks.async {
                    QueueHandler.saveQueue(queue)
                }
            }
        }
    }

    private inner class FlushButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.DARK_RED}${ChatColor.BOLD}Flush Queue"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("")

            description.addAll(TextSplitter.split(
                length = 40,
                text = "Flushing the queue will remove all players from the queue.",
                linePrefix = ChatColor.GRAY.toString()
            ))

            description.add("")
            description.add("${ChatColor.DARK_RED}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.DARK_RED}to flush queue")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.TNT
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                ConfirmMenu { confirmed ->
                    if (confirmed) {
                        queue.flush()
                    } else {
                        player.sendMessage("${ChatColor.YELLOW}No changes made.")
                    }

                    this@EditQueueMenu.openMenu(player)
                }.openMenu(player)
            }
        }
    }

}