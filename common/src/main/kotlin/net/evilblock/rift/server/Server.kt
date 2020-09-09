package net.evilblock.rift.server

import java.util.*
import kotlin.collections.HashMap

class Server(val id: String, val group: String, val port: Int) {

    var displayName: String = id
    var slots: Int = 0
    var whitelisted: Boolean = false
    var onlineMode: Boolean = false
    var proxied: Boolean = false

    var lastHeartbeat: Long = 0
    var currentUptime: Long = 0
    var currentTps: Double = 0.toDouble()
    var playerCount: Int = 0

    /**
     * Initializes a [Server] from the given key-specific populated [map].
     */
    constructor(map: Map<String, String>) : this(
        map.getValue("ID"),
        map.getValue("Group"),
        map.getValue("Port").toInt()
    ) {
        displayName = map.getValue("DisplayName")
        slots = map.getValue("Slots").toInt()
        whitelisted = map.getValue("Whitelisted").toBoolean()
        onlineMode = map.getValue("OnlineMode").toBoolean()
        proxied = map.getValue("Proxied").toBoolean()
        lastHeartbeat = map.getValue("LastHeartbeat").toLong()
        currentUptime = map.getValue("CurrentUptime").toLong()
        currentTps = map.getValue("CurrentTPS").toDouble()
        playerCount = map.getValue("PlayerCount").toInt()
    }

    /**
     * Gets a map populated with this server's key-specific data.
     */
    fun toMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map["ID"] = id
        map["DisplayName"] = displayName
        map["Group"] = group
        map["Port"] = port.toString()
        map["Slots"] = slots.toString()
        map["Whitelisted"] = whitelisted.toString()
        map["OnlineMode"] = onlineMode.toString()
        map["Proxied"] = proxied.toString()
        map["LastHeartbeat"] = lastHeartbeat.toString()
        map["CurrentUptime"] = currentUptime.toString()
        map["CurrentTPS"] = currentTps.toString()
        map["PlayerCount"] = playerCount.toString()
        return map
    }

    /**
     * If the server is considered online. If the last heartbeat time of this server exceeds 5 seconds,
     * the server is considered offline.
     */
    fun isOnline(): Boolean {
        return (System.currentTimeMillis() - lastHeartbeat) < 5000L
    }

    /**
     * Gets the current uptime in milliseconds. If the server is considered offline ([Server.isOnline]),
     * an empty value is returned.
     */
    fun getCurrentUptime(): Optional<Long> {
        return if (isOnline()) {
            Optional.of(currentUptime)
        } else {
            Optional.empty()
        }
    }

    /**
     * Gets the current TPS. If the server is considered offline ([Server.isOnline]), an empty value is returned.
     */
    fun getCurrentTps(): Optional<Double> {
        return if (isOnline()) {
            Optional.of(currentTps)
        } else {
            Optional.empty()
        }
    }

    /**
     * Gets the current player count. If the server is considered offline ([Server.isOnline]), an empty
     * value is returned.
     */
    fun getPlayerCount(): Optional<Int> {
        return if (isOnline()) {
            Optional.of(playerCount)
        } else {
            Optional.empty()
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Server && other.id == this.id && other.port == this.port
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + port
        return result
    }

}