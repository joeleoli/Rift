package net.evilblock.rift.bukkit.queue.serializer

import com.google.gson.*
import net.evilblock.rift.queue.Queue
import net.evilblock.rift.queue.QueueHandler
import java.lang.reflect.Type

class QueueReferenceSerializer : JsonSerializer<Queue>, JsonDeserializer<Queue> {

    override fun serialize(server: Queue, type: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(server.id)
    }

    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Queue? {
        return QueueHandler.getQueueById(json.asString)
    }

}