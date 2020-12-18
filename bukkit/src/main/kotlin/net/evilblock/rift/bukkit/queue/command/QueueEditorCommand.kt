package net.evilblock.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import net.evilblock.rift.bukkit.queue.menu.QueueEditorMenu
import net.evilblock.rift.bukkit.util.Permissions
import org.bukkit.entity.Player

object QueueEditorCommand {

    @Command(
        names = ["queue editor", "rift queue editor"],
        description = "Opens the queue editor",
        permission = Permissions.QUEUE_EDITOR
    )
    @JvmStatic
    fun execute(player: Player) {
        QueueEditorMenu().openMenu(player)
    }

}