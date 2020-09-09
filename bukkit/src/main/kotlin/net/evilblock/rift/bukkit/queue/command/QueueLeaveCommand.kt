package net.evilblock.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.rift.Rift
import net.evilblock.rift.bukkit.RiftBukkitPlugin
import net.evilblock.rift.queue.Queue
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object QueueLeaveCommand {

    @Command(
        names = ["queue leave", "ql", "leavequeue"],
        description = "Leave the queue you're in",
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "queue", defaultValue = "self") queue: Queue) {
        val entry = queue.getEntry(player.uniqueId)
        if (entry == null) {
            player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}You are not in that queue.")
            return
        }

        queue.removeEntry(entry)
        Rift.instance.plugin.onLeaveQueue(queue, entry)
    }

}