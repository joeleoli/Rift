package com.minexd.rift.bukkit.queue.event

import net.evilblock.cubed.plugin.PluginEvent
import com.minexd.rift.queue.Queue
import org.bukkit.entity.Player

open class PlayerLeaveQueueEvent(val player: Player, val queue: Queue) : PluginEvent()