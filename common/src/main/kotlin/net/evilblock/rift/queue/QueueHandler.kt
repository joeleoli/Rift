package net.evilblock.rift.queue

import net.evilblock.pidgin.message.Message
import net.evilblock.rift.Rift
import java.util.*

object QueueHandler {

    const val PRIORITY_UPDATE = "PRIORITY_UPDATE"

    private val queues: MutableMap<String, Queue> = hashMapOf()
    private var priority: MutableMap<String, Int> = hashMapOf()

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
        Rift.instance.runRedisCommand { redis ->
            for (queueId in redis.smembers("Rift:Queues")) {
                fetchQueueById(queueId).ifPresent { queue ->
                    queues[queue.id.toLowerCase()] = queue
                    queue.cachedEntries = queue.fetchEntries()
                }
            }
        }
    }

    /**
     * Fetches a queue by querying redis.
     */
    fun fetchQueueById(queueId: String): Optional<Queue> {
        return Optional.ofNullable(Rift.instance.runRedisCommand { client ->
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
        })
    }

    /**
     * Saves a [queue] to redis.
     */
    fun saveQueue(queue: Queue) {
        Rift.instance.runRedisCommand { redis ->
            redis.sadd("Rift:Queues", queue.id)
            redis.hmset("Rift:Queue:${queue.id}", queue.toMap())
        }

        queue.sendUpdateMessage()
    }

    /**
     * Deletes a [queue] from redis.
     */
    fun deleteQueue(queue: Queue) {
        Rift.instance.runRedisCommand { redis ->
            redis.srem("Rift: Queues", queue.id)
            redis.del("Rift:Queue:${queue.id}")
        }

        Rift.instance.pidgin.sendMessage(Message(Queue.QUEUE_DELETE, mapOf("ID" to queue.id)))
    }

    fun getPriority(): MutableMap<String, Int> {
        return priority
    }

    /**
     * Loads the priority map from redis.
     */
    fun loadPriority() {
        Rift.instance.runRedisCommand { redis ->
            if (redis.exists("Rift:QueuePriority")) {
                priority = redis.hgetAll("Rift:QueuePriority").map { it.key to it.value.toInt() }.toMap().toMutableMap()
            }
        }
    }

    /**
     * Saves the given priority entry to the priority map in redis.
     */
    fun savePriority(permission: String, priority: Int) {
        this.priority[permission.toLowerCase()] = priority

        Rift.instance.runRedisCommand { redis ->
            redis.hmset("Rift:QueuePriority", mapOf(permission.toLowerCase() to priority.toString()))
        }

        Rift.instance.pidgin.sendMessage(Message(PRIORITY_UPDATE))
    }

    fun deletePriority(permission: String) {
        this.priority.remove(permission.toLowerCase())

        Rift.instance.runRedisCommand { redis ->
            redis.hdel("Rift:QueuePriority", permission.toLowerCase())
        }

        Rift.instance.pidgin.sendMessage(Message(PRIORITY_UPDATE))
    }

}