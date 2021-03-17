package com.minexd.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.rift.Rift
import com.minexd.rift.queue.Queue
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object QueueLeaveCommand {

    @Command(
        names = ["queue leave", "leave-queue", "leavequeue"],
        description = "Leave the queue you're in",
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "queue", defaultValue = "self") queue: Queue) {
        val entry = queue.getEntry(player.uniqueId)
        if (entry == null) {
            player.sendMessage("${ChatColor.RED}You are not in that queue!")
            return
        }

        queue.removeEntry(entry)
        Rift.instance.plugin.onLeaveQueue(queue, entry)
    }

}