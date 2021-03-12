package com.minexd.rift.server

import com.google.gson.JsonObject
import net.evilblock.cubed.serializers.Serializers

class ServerGroup(val id: String) {

    var displayName: String = id
    val servers: MutableSet<Server> = HashSet()
    var configuration: JsonObject = JsonObject()

    constructor(map: Map<String, String>) : this(map.getValue("ID")) {
        displayName = map.getValue("DisplayName")
        configuration = Serializers.gson.fromJson(map.getValue("Configuration"), JsonObject::class.java)
    }

    fun toMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map["ID"] = id
        map["DisplayName"] = displayName
        map["Configuration"] = configuration.toString()
        return map
    }

    fun getOnlineServers(): List<Server> {
        return servers.filter { it.isOnline() }
    }

    fun getOfflineServers(): List<Server> {
        return servers.filter { !it.isOnline() }
    }

    fun getPublicServers(): List<Server> {
        return servers.filter { it.isOnline() && !it.whitelisted }
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
