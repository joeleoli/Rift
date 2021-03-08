package com.minexd.rift.queue

import net.evilblock.pidgin.message.Message
import com.minexd.rift.Rift
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object QueueHandler {

    const val PRIORITY_UPDATE = "PriorityUpdate"
    const val QUEUE_UPDATE = "QueueUpdate"
    const val QUEUE_DELETE = "QueueDelete"
    const val QUEUE_FLUSH = "QueueFlush"
    const val QUEUE_ADD_ENTRY = "QueueAddEntry"
    const val QUEUE_REMOVE_ENTRY = "QueueRemoveEntry"

    private val queues: MutableMap<String, Queue> = ConcurrentHashMap()
    private var priority: MutableMap<String, Int> = ConcurrentHashMap()

    fun initialLoad() {
        loadQueues()
        loadPriority()
    }

    fun getQueues(): Collection<Queue> {
        return queues.values
    }

    fun trackQueue(queue: Queue) {
        queues[queue.id.toLowerCase()] = queue
    }

    fun forgetQueue(queue: Queue) {
        queues.remove(queue.id.toLowerCase())
    }

    fun getQueueById(queueId: String): Queue? {
        return queues[queueId.toLowerCase()]
    }

    fun getQueueByEntry(entryId: UUID): Queue? {
        return queues.values.firstOrNull { it.getEntry(entryId) != null }
    }

    /**
     * Loads the [queues] map from redis.
     */
    private fun loadQueues() {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            for (queueId in redis.smembers("Rift:Queues")) {
                val queue = fetchQueueById(queueId) ?: continue
                queues[queue.id.toLowerCase()] = queue
                queue.cachedEntries = queue.fetchEntries()
            }
        }
    }

    /**
     * Fetches a queue by querying redis.
     */
    fun fetchQueueById(queueId: String): Queue? {
        return Rift.instance.plugin.getRedis().runRedisCommand { client ->
            val map = client.hgetAll("Rift:Queue:$queueId")
            val queue = if (map.isEmpty()) {
                null
            } else {
                Queue(map)
            }

            if (queue != null) {
                queue.cachedEntries = queue.fetchEntries()
            }

            queue
        }
    }

    /**
     * Saves a [queue] to redis.
     */
    fun saveQueue(queue: Queue) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.sadd("Rift:Queues", queue.id)
            redis.hmset("Rift:Queue:${queue.id}", queue.toMap())
        }

        Rift.instance.pidgin.sendMessage(Message(QUEUE_UPDATE, mapOf("Queue" to queue.id)))
    }

    /**
     * Deletes a [queue] from redis.
     */
    fun deleteQueue(queue: Queue) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.srem("Rift:Queues", queue.id)
            redis.del("Rift:Queue:${queue.id}")
        }

        Rift.instance.pidgin.sendMessage(Message(QUEUE_DELETE, mapOf("ID" to queue.id)))
    }

    fun getPriority(): MutableMap<String, Int> {
        return priority
    }

    /**
     * Loads the priority map from redis.
     */
    fun loadPriority() {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            if (redis.exists("Rift:QueuePriority")) {
                priority = redis.hgetAll("Rift:QueuePriority").map { it.key to it.value.toInt() }.toMap().toMutableMap()
            }
        }
    }

    /**
     * Saves the given priority entry to the priority map in redis.
     */
    fun savePriority(permission: String, priority: Int) {
        QueueHandler.priority[permission.toLowerCase()] = priority

        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.hmset("Rift:QueuePriority", mapOf(permission.toLowerCase() to priority.toString()))
        }

        Rift.instance.pidgin.sendMessage(Message(PRIORITY_UPDATE))
    }

    fun deletePriority(permission: String) {
        priority.remove(permission.toLowerCase())

        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.hdel("Rift:QueuePriority", permission.toLowerCase())
        }

        Rift.instance.pidgin.sendMessage(Message(PRIORITY_UPDATE))
    }

}