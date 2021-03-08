package com.minexd.rift.proxy

class Proxy(val id: String) {

    var displayName: String = id
    val servers: MutableList<String> = arrayListOf()

    var lastHeartbeat: Long = 0
    var currentUptime: Long = 0
    var playerCount: Int = 0

    /**
     * Initializes a [Proxy] from the given key-specific populated [map].
     */
    constructor(map: Map<String, String>) : this(map.getValue("ID")) {
        displayName = map.getValue("DisplayName")
        lastHeartbeat = map.getValue("LastHeartbeat").toLong()
        currentUptime = map.getValue("CurrentUptime").toLong()
        playerCount = map.getValue("PlayerCount").toInt()
    }

    /**
     * Gets a map populated with this [Proxy]'s key-specific data.
     */
    fun toMap(): Map<String, String> {
        return hashMapOf<String, String>().also { map ->
            map["ID"] = id
            map["DisplayName"] = displayName
            map["LastHeartbeat"] = lastHeartbeat.toString()
            map["CurrentUptime"] = currentUptime.toString()
            map["PlayerCount"] = playerCount.toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Proxy && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}