package com.minexd.rift.bukkit.queue.command.parameter

import net.evilblock.cubed.command.data.parameter.ParameterType
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QueueParameterType : ParameterType<Queue> {

    override fun transform(sender: CommandSender, source: String): Queue? {
        if (sender is Player && source.equals("self", ignoreCase = true)) {
            val queue = QueueHandler.getQueueByEntry(sender.uniqueId)

            if (queue == null) {
                sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}You are not in a queue.")
            }

            return queue
        }

        val queue = QueueHandler.getQueueById(source)

        if (queue == null) {
            sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}QUEUE ${ChatColor.RED}Couldn't find a queue named `$source`.")
        }

        return queue
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        val completions = arrayListOf<String>()

        for (queue in QueueHandler.getQueues()) {
            if (queue.id.startsWith(source, ignoreCase = true)) {
                completions.add(queue.id)
            }
        }

        return completions
    }

}