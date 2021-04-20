package com.minexd.rift.queue

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.minexd.rift.Rift
import net.evilblock.cubed.serializers.Serializers
import java.util.*

object QueueMessages : MessageListener {

    private val TYPE = TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).rawType

    @IncomingMessageHandler(id = QueueHandler.QUEUE_UPDATE)
    fun onUpdate(data: JsonObject) {
        val queue = QueueHandler.fetchQueueById(data["Queue"].asString) ?: return
        QueueHandler.trackQueue(queue)
    }

    @IncomingMessageHandler(id = QueueHandler.QUEUE_DELETE)
    fun onDelete(data: JsonObject) {
        val queue = QueueHandler.getQueueById(data.get("Queue").asString) ?: return
        QueueHandler.forgetQueue(queue)
    }

    @IncomingMessageHandler(id = QueueHandler.QUEUE_FLUSH)
    fun onFlush(data: JsonObject) {
        val queue = QueueHandler.getQueueById(data.get("Queue").asString) ?: return
        queue.cachedEntries.clear()
    }

    @IncomingMessageHandler(id = QueueHandler.QUEUE_ADD_ENTRY)
    fun onAddEntry(data: JsonObject) {
        val map = Serializers.gson.fromJson<Map<String, String>>(data, TYPE) as Map<String, String>
        val queue = QueueHandler.getQueueById(map.getValue("QueueID")) ?: return

        val entry = QueueEntry(map)
        queue.cachedEntries.add(entry)
        queue.recalculateEntryPositions()

        Rift.instance.plugin.onJoinQueue(queue, entry)
    }

    @IncomingMessageHandler(id = QueueHandler.QUEUE_REMOVE_ENTRY)
    fun onRemoveEntry(data: JsonObject) {
        val queue = QueueHandler.getQueueById(data.get("QueueID").asString) ?: return

        val entry = queue.getEntry(UUID.fromString(data.get("EntryID").asString)) ?: return
        queue.cachedEntries.remove(entry)
        queue.recalculateEntryPositions()

        Rift.instance.plugin.onLeaveQueue(queue, entry)
    }

    @IncomingMessageHandler(id = QueueHandler.PRIORITY_UPDATE)
    fun onPriorityUpdate(data: JsonObject) {
        QueueHandler.loadPriority()
    }

}