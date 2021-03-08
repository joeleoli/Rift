package com.minexd.rift.server

import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.evilblock.cubed.serializers.Serializers

object ServerMessages : MessageListener {

    private val TYPE = TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).rawType

    @IncomingMessageHandler(ServerHandler.GROUP_UPDATE)
    fun onGroupUpdate(data: JsonObject) {
        val map = Serializers.gson.fromJson<Map<String, String>>(data, TYPE) as Map<String, String>

        val group = ServerHandler.getGroupById(map.getValue("Group")) ?: ServerGroup(map)
        group.configuration = Serializers.gson.fromJson(map.getValue("Configuration"), JsonObject::class.java)
    }

    @IncomingMessageHandler(ServerHandler.SERVER_UPDATE)
    fun onServerUpdate(data: JsonObject) {
        ServerHandler.loadOrCreateServer(data["Server"].asString, data["ServerPort"].asInt)
    }

}