package com.minexd.rift.bukkit.server.menu

import com.minexd.rift.bukkit.server.group.menu.SelectGroupMenu
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class EditServerMenu(private val origin: Menu?, private val server: Server) : Menu() {

    init {
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Edit Server - ${server.displayName}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = EditDisplayNameButton()
            buttons[1] = EditMetadataButton()
            buttons[2] = EditGroupButton()
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose && origin != null) {
            Tasks.delayed(1L) {
                origin.openMenu(player)
            }
        }
    }

    private inner class EditDisplayNameButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Display Name"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also { desc ->
                desc.add("")
                desc.addAll(TextSplitter.split(text = "Edit the server's display name, which appears in menu and chat text."))
                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit display anem"))
            }
        }

        override fun getMaterial(player: Player): Material {
            return Material.NAME_TAG
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                InputPrompt()
                    .withText("${ChatColor.GREEN}Please input the server's new display name.")
                    .acceptInput { input ->
                        server.displayName = input

                        Tasks.async {
                            ServerHandler.saveServer(server)
                        }

                        this@EditServerMenu.openMenu(player)
                    }
                    .start(player)
            }
        }
    }

    private inner class EditMetadataButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Metadata"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also { desc ->
                desc.add("")
                desc.addAll(TextSplitter.split(text = "Edit this server's metadata, a key-value system that can store any data to be used by a plugin."))
                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit group"))
            }
        }

        override fun getMaterial(player: Player): Material {
            return Material.BOOK_AND_QUILL
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                MetadataEditorMenu(server).openMenu(player)
            }
        }
    }

    private inner class EditGroupButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Edit Group"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also { desc ->
                desc.add("")
                desc.addAll(TextSplitter.split(text = "Edit the group that this server belongs to."))
                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit group"))
            }
        }

        override fun getMaterial(player: Player): Material {
            return Material.HOPPER
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                SelectGroupMenu { group ->
                    if (group != null) {
                        ServerHandler.getGroupById(server.group)?.servers?.remove(server) // remove server from old group

                        server.group = group.id

                        Tasks.async {
                            ServerHandler.saveServer(server)
                        }
                    }

                    this@EditServerMenu.openMenu(player)
                }.openMenu(player)
            }
        }
    }

}