package net.evilblock.rift.bukkit.queue.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.rift.bukkit.RiftBukkitPlugin
import net.evilblock.rift.queue.Queue
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object QueueJoinCommand {

    @Command(
        names = ["queue join", "qj", "joinqueue"],
        description = "Join a queue",
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "queue") queue: Queue) {
        if (RiftBukkitPlugin.instance.readDisableJoinQueueCommand() && !player.hasPermission("rift.queue.join")) {
            player.sendMessage("${ChatColor.RED}You aren't allowed to join queues using commands!")
            return
        }

        RiftBukkitPlugin.instance.joinQueue(player, queue)
    }

}