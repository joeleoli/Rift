package com.minexd.rift.queue

import java.util.*

class QueueEntry(val uuid: UUID, val priority: Int) {

    var insertTime: Long = System.currentTimeMillis()
    var expiresAt: Long? = null
    var position: Int = -1
    var spoofed: Boolean = false

    constructor(map: Map<String, String>) : this(
        UUID.fromString(map.getValue("ID")),
        map.getValue("Priority").toInt()
    ) {
        insertTime = map.getValue("InsertTime").toLong()
        spoofed = map.getValue("Spoofed").toBoolean()
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "ID" to uuid.toString(),
            "Priority" to priority.toString(),
            "InsertTime" to insertTime.toString(),
            "Spoofed" to spoofed.toString()
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is QueueEntry && other.uuid == this.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

}