package net.evilblock.rift.server

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.ArrayList
import java.util.HashMap

class ServerGroup(val id: String) {

    companion object {
        private val PARSER = JsonParser()
    }

    var displayName: String = id
    val servers: MutableSet<Server> = HashSet()
    var configuration: JsonObject = JsonObject()

    constructor(map: Map<String, String>) : this(map.getValue("ID")) {
        displayName = map.getValue("DisplayName")
        configuration = PARSER.parse(map.getValue("Configuration")).asJsonObject
    }

    fun toMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map["ID"] = id
        map["DisplayName"] = displayName
        map["Configuration"] = configuration.toString()
        return map
    }

    /**
     * Sums the player count of all the [servers] in this group.
     */
    fun getAllServersPlayerCount(): Int {
        return servers.stream().mapToInt { server -> server.getPlayerCount().orElse(0) }.sum()
    }

    /**
     * Sums the player count of all the [servers] in this group that are online.
     */
    fun getOnlineServersPlayerCount(): Int {
        return servers.stream().mapToInt { server -> if (server.isOnline()) 1 else 0 }.sum()
    }

}
