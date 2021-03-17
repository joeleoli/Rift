package com.minexd.rift.queue

import net.evilblock.pidgin.message.Message
import com.minexd.rift.Rift
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import java.util.*

class Queue(
    val id: String,
    var route: Server
) {

    var open: Boolean = false
    var polling: Boolean = false
    var pollingRate: Double = 2.0
    var pollingSize: Int = 3

    var lastPoll: Long = -1
    var cachedEntries: MutableSet<QueueEntry> = hashSetOf()

    /**
     * Initializes a [Queue] from the given key-specific populated [map].
     */
    constructor(map: Map<String, String>) : this(map.getValue("ID"), ServerHandler.getServerById(map.getValue("Route"))!!) {
        open = map.getValue("Open").toBoolean()
        polling = map.getValue("Polling").toBoolean()
        pollingRate = map.getValue("PollingRate").toDouble()
        pollingSize = map.getValue("PollingSize").toInt()
    }

    /**
     * Gets a map populated with this queue's key-specific data.
     */
    fun toMap(): Map<String, String> {
        return mapOf(
            "ID" to id,
            "Route" to route.id,
            "Open" to open.toString(),
            "Polling" to polling.toString(),
            "PollingRate" to pollingRate.toString(),
            "PollingSize" to pollingSize.toString()
        )
    }

    fun getEntry(player: UUID): QueueEntry? {
        return cachedEntries.firstOrNull { entry -> entry.uuid.toString() == player.toString() }
    }

    fun addEntry(player: UUID, priority: Int) {
        val entry = QueueEntry(player, priority)
        cachedEntries.add(entry)

        recalculateEntryPositions()

        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.sadd("Rift:Queue:${id}.Entries", player.toString())
            redis.hmset("Rift:Queue:${id}.Entry:${player}", entry.toMap())
        }

        val messageData = entry.toMap().toMutableMap().also { it["QueueID"] = id }
        Rift.instance.pidgin.sendMessage(Message(QueueHandler.QUEUE_ADD_ENTRY, messageData))
    }

    fun removeEntry(entry: QueueEntry) {
        cachedEntries.remove(entry)

        recalculateEntryPositions()

        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.srem("Rift:Queue:${id}.Entries", entry.uuid.toString())
            redis.del("Rift:Queue:${id}.Entry:${entry.uuid}")
        }

        Rift.instance.pidgin.sendMessage(Message(QueueHandler.QUEUE_REMOVE_ENTRY, mapOf(
            "QueueID" to id,
            "EntryID" to entry.uuid.toString()
        )))
    }

    private fun fetchEntry(player: UUID): QueueEntry? {
        return Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            if (!redis.exists("Rift:Queue:${id}.Entry:${player}")) {
                return@runRedisCommand null
            }

            val map = redis.hgetAll("Rift:Queue:${id}.Entry:${player}")
            if (map == null) {
                null
            } else {
                QueueEntry(map)
            }
        }
    }

    fun fetchEntries(): MutableSet<QueueEntry> {
        return Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            val entries = redis.smembers("Rift:Queue:${id}.Entries")
                .mapNotNull { fetchEntry(UUID.fromString(it)) }
                .sortedWith(ENTRY_COMPARATOR)
                .toMutableSet()

            for (entry in entries) {
                entry.position = entries.indexOf(entry) + 1
            }

            entries
        }
    }

    fun getSortedEntries(): MutableList<QueueEntry> {
        return cachedEntries.sortedWith(ENTRY_COMPARATOR).toMutableList()
    }

    fun recalculateEntryPositions() {
        val sortedEntries = getSortedEntries()
        for (update in sortedEntries) {
            update.position = sortedEntries.indexOf(update) + 1
        }
    }

    fun flush() {
        cachedEntries.clear()

        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            for (player in redis.smembers("Rift:Queue:${id}.Entries")) {
                redis.del("Rift:Queue:${id}.Entry:${player}")
            }

            redis.del("Rift:Queue:${id}.Entries")
        }

        Rift.instance.pidgin.sendMessage(Message(QueueHandler.QUEUE_FLUSH, mapOf("Queue" to id)))
    }

    fun canPoll(): Boolean {
        return (lastPoll == -1L || System.currentTimeMillis() >= lastPoll + (pollingRate * 1000.0))
                && cachedEntries.size > 0
                && polling
                && route.isOnline()
                && !route.whitelisted
                && route.proxied
                && route.playerCount < route.slots
    }

    companion object {
        private val ENTRY_COMPARATOR = Comparator<QueueEntry> { o1, o2 ->
            if (o2.priority > o1.priority) {
                return@Comparator 1
            }

            if (o1.priority == o2.priority) {
                return@Comparator (o2.insertTime - o1.insertTime).toInt()
            }

            return@Comparator -1
        }
    }

}