package com.minexd.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import com.minexd.rift.queue.QueueHandler
import org.bukkit.command.CommandSender

object PriorityDebugCommand {

    @Command(
        names = ["queue priority-debug"],
        description = "Debug priority",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        sender.sendMessage("Priorities:")

        for ((permission, priority) in QueueHandler.getPriority().map { Pair(it.key, it.value) }.sortedWith(Comparator { o1, o2 ->
            if (o2.second > o1.second) {
                return@Comparator 1
            }

            return@Comparator -1
        })) {
            sender.sendMessage("$permission - $priority")
        }
    }

}