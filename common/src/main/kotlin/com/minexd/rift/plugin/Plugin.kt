package com.minexd.rift.plugin

import net.evilblock.cubed.store.redis.Redis
import com.minexd.rift.queue.Queue
import com.minexd.rift.queue.QueueEntry
import java.io.File
import java.util.logging.Logger

interface Plugin {

    fun getLogger(): Logger

    fun getDirectory(): File

    fun getRedis(): Redis

    fun isProxy(): Boolean

    fun onJoinQueue(queue: Queue, entry: QueueEntry) {

    }

    fun onLeaveQueue(queue: Queue, entry: QueueEntry) {

    }

}