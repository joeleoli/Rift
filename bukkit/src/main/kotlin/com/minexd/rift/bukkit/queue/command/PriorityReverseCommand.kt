package com.minexd.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import com.minexd.rift.queue.QueueHandler
import org.bukkit.command.CommandSender

object PriorityReverseCommand {

    @Command(
        names = ["queue priority-reverse"],
        description = "Reverse priority",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        sender.sendMessage("Priorities:")

        for ((index, pair) in QueueHandler.getPriority().map { Pair(it.key, it.value) }.sortedWith(Comparator { o1, o2 ->
            if (o2.second > o1.second) {
                return@Comparator 1
            }

            return@Comparator -1
        }).reversed().withIndex()) {
            QueueHandler.savePriority(pair.first, index)
        }
    }

}