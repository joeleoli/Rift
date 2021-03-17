package com.minexd.rift.bukkit.server.serializer

import com.google.gson.*
import com.minexd.rift.server.Server
import com.minexd.rift.server.ServerHandler
import java.lang.reflect.Type

class ServerReferenceSerializer : JsonSerializer<Server>, JsonDeserializer<Server> {

    override fun serialize(server: Server, type: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(server.id)
    }

    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Server? {
        return ServerHandler.getServerById(json.asString, true)
    }

}