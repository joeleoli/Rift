package net.evilblock.rift.bukkit.queue.event

import net.evilblock.cubed.plugin.PluginEvent
import net.evilblock.rift.queue.Queue
import org.bukkit.entity.Player

open class PlayerLeaveQueueEvent(val player: Player, val queue: Queue) : PluginEvent()