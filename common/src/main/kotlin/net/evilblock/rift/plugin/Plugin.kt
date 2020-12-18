package net.evilblock.rift.plugin

import net.evilblock.rift.queue.Queue
import net.evilblock.rift.queue.QueueEntry
import redis.clients.jedis.JedisPool
import java.io.File
import java.util.logging.Logger

interface Plugin {

    fun getLogger(): Logger

    fun getDirectory(): File

    fun getJedisPool(): JedisPool

    fun isProxy(): Boolean

    fun onJoinQueue(queue: Queue, entry: QueueEntry) {

    }

    fun onLeaveQueue(queue: Queue, entry: QueueEntry) {

    }

}