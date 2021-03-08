package com.minexd.rift.bukkit.server.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class MetadataEditorMenu(private val server: Server) : Menu() {

    init {
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Metadata Editor - ${server.id}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        buttons[0] = CreateKeyButton()

        for ((key, value) in server.configuration.entrySet()) {
            buttons[buttons.size] = MetadataEntryButton(key, value.toString())
        }

        return buttons
    }

    private inner class CreateKeyButton : AddButton() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Create Metadata Key"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also {
                it.add("")
                it.addAll(TextSplitter.split(linePrefix = ChatColor.GRAY.toString(), text = "Create a new key and value for this server's metadata/configuration."))
                it.add("")
                it.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to create key")
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                EzPrompt.Builder()
                    .promptText("${ChatColor.GREEN}Please input the new key.")
                    .regex("[a-zA-Z-.]*".toRegex())
                    .charLimit(80)
                    .acceptInput { input ->
                        if (server.configuration.has(input)) {
                            player.sendMessage("${ChatColor.RED}That server's metadata already has a \"$input\" key!")
                            return@acceptInput
                        }

                        UpdateValueMenu(input).openMenu(player)
                    }
                    .build()
                    .start(player)
            }
        }
    }

    private inner class MetadataEntryButton(private val key: String, private val value: String) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.GRAY}Key: ${ChatColor.YELLOW}${key}"
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()

            description.add("${ChatColor.GRAY}Value: ${ChatColor.WHITE}$value")
            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK ${ChatColor.GREEN}to edit value")
            description.add("${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK ${ChatColor.RED}to delete key")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.PAPER
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                UpdateValueMenu(key).openMenu(player)
            } else if (clickType.isRightClick) {
                ConfirmMenu("Delete key \"$key\"?") { confirmed ->
                    if (confirmed) {
                        server.configuration.remove(key)

                        Tasks.async {
                            ServerHandler.saveServer(server)
                        }
                    }

                    this@MetadataEditorMenu.openMenu(player)
                }.openMenu(player)
            }
        }
    }

    private inner class UpdateValueMenu(private val key: String) : Menu() {
        override fun getTitle(player: Player): String {
            return "Select Value Type"
        }

        override fun getButtons(player: Player): Map<Int, Button> {
            return hashMapOf<Int, Button>().also {
                it[2] = StringTypeButton()
                it[6] = NumberTypeButton()

                for (i in 0 until 9) {
                    if (!it.containsKey(i)) {
                        it[i] = GlassButton(7)
                    }
                }
            }
        }

        override fun onClose(player: Player, manualClose: Boolean) {
            if (manualClose) {
                Tasks.delayed(1L) {
                    this@MetadataEditorMenu.openMenu(player)
                }
            }
        }

        private inner class StringTypeButton : Button() {
            override fun getName(player: Player): String {
                return "${ChatColor.YELLOW}${ChatColor.BOLD}String Type"
            }

            override fun getMaterial(player: Player): Material {
                return Material.SIGN
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                if (clickType.isLeftClick) {
                    EzPrompt.Builder()
                        .promptText("${ChatColor.GREEN}Please input a new string value.")
                        .acceptInput { input ->
                            server.configuration.addProperty(key, input)

                            Tasks.async {
                                ServerHandler.saveServer(server)
                            }

                            this@MetadataEditorMenu.openMenu(player)
                        }
                        .build()
                        .start(player)
                }
            }
        }

        private inner class NumberTypeButton : TexturedHeadButton(NUMBER_HEAD_TEXTURE) {
            override fun getName(player: Player): String {
                return "${ChatColor.YELLOW}${ChatColor.BOLD}Number Type"
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                if (clickType.isLeftClick) {
                    NumberPrompt()
                        .withText("${ChatColor.GREEN}Please input a new number value.")
                        .acceptInput{ number ->
                            server.configuration.addProperty(key, number)

                            Tasks.async {
                                ServerHandler.saveServer(server)
                            }

                            this@MetadataEditorMenu.openMenu(player)
                        }.start(player)
                }
            }
        }
    }

    companion object {
        private const val NUMBER_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFhOTQ2M2ZkM2M0MzNkNWUxZDlmZWM2ZDVkNGIwOWE4M2E5NzBiMGI3NGRkNTQ2Y2U2N2E3MzM0OGNhYWIifX19"
    }

}