package net.evilblock.rift.server

import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.evilblock.rift.Rift

object ServerMessages : MessageListener {

    private val TYPE = TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).rawType

    @IncomingMessageHandler(ServerHandler.SERVER_GROUP_UPDATE)
    fun onGroupUpdate(json: JsonObject) {
        val map = Rift.GSON.fromJson<Map<String, String>>(json, TYPE) as Map<String, String>

        val optionalGroup = ServerHandler.getGroupById(map.getValue("Group"))
        if (optionalGroup.isPresent) {
            optionalGroup.get().configuration = Rift.GSON.fromJson(map.getValue("Configuration"), JsonObject::class.java)
        } else {
            val group = ServerGroup(map)
            ServerHandler.groups[group.displayName] = group
        }
    }

    @IncomingMessageHandler(ServerHandler.SERVER_UPDATE)
    fun onUpdate(json: JsonObject) {
        val map = Rift.GSON.fromJson<Map<String, String>>(json, TYPE) as Map<String, String>

        val optionalServer = ServerHandler.getServerById(map.getValue("ID"))
        if (optionalServer.isPresent) {
            val server = optionalServer.get()
            server.displayName = map.getValue("DisplayName")
            server.slots = map.getValue("Slots").toInt()
            server.whitelisted = map.getValue("Whitelisted").toBoolean()
            server.onlineMode = map.getValue("OnlineMode").toBoolean()
            server.proxied = map.getValue("Proxied").toBoolean()
            server.lastHeartbeat = map.getValue("LastHeartbeat").toLong()
            server.currentUptime = map.getValue("CurrentUptime").toLong()
            server.currentTps = map.getValue("CurrentTPS").toDouble()
            server.playerCount = map.getValue("PlayerCount").toInt()
        } else {
            ServerHandler.loadOrCreateServer(map.getValue("ID"), map.getValue("Port").toInt())
        }
    }

}