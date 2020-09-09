package net.evilblock.rift.queue

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.evilblock.rift.Rift
import net.evilblock.rift.server.ServerHandler
import java.util.*

object QueueMessages : MessageListener {

    private val TYPE = TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).rawType

    @IncomingMessageHandler(id = Queue.QUEUE_UPDATE)
    fun onUpdate(data: JsonObject) {
        val map = Rift.GSON.fromJson<Map<String, String>>(data, TYPE) as Map<String, String>

        val queue = QueueHandler.getQueueById(map.getValue("ID"))
        if (queue != null) {
            ServerHandler.getServerById(map.getValue("Route")).ifPresent { route ->
                queue.route = route
                queue.open = map.getValue("Open").toBoolean()
                queue.polling = map.getValue("Polling").toBoolean()
                queue.pollingRate = map.getValue("PollingRate").toDouble()
                queue.pollingSize = map.getValue("PollingSize").toInt()
            }
        } else {
            QueueHandler.trackQueue(Queue(map))
        }
    }

    @IncomingMessageHandler(id = Queue.QUEUE_DELETE)
    fun onDelete(data: JsonObject) {
        val queue = QueueHandler.getQueueById(data.get("ID").asString) ?: return
        QueueHandler.forgetQueue(queue)
    }

    @IncomingMessageHandler(id = Queue.QUEUE_FLUSH)
    fun onFlush(data: JsonObject) {
        val queue = QueueHandler.getQueueById(data.get("ID").asString) ?: return
        queue.cachedEntries.clear()
    }

    @IncomingMessageHandler(id = Queue.QUEUE_ADD_ENTRY)
    fun onAddEntry(data: JsonObject) {
        val map = Rift.GSON.fromJson<Map<String, String>>(data, TYPE) as Map<String, String>
        val queue = QueueHandler.getQueueById(map.getValue("QueueID")) ?: return

        val entry = QueueEntry(map)
        queue.cachedEntries.add(entry)
        queue.recalculateEntryPositions()

        Rift.instance.plugin.onJoinQueue(queue, entry)
    }

    @IncomingMessageHandler(id = Queue.QUEUE_REMOVE_ENTRY)
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