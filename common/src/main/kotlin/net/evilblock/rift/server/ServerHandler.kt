package net.evilblock.rift.server

import net.evilblock.rift.Rift
import net.evilblock.pidgin.message.Message
import java.util.*

object ServerHandler {

    const val SERVER_GROUP_UPDATE = "SERVER_GROUP_UPDATE"
    const val SERVER_UPDATE = "SERVER_UPDATE"

    val groups: MutableMap<String, ServerGroup> = hashMapOf()

    /**
     *  The initial load procedure goes as follows:
     *  1. Load server groups into memory
     *  2. Load servers and pair them with their assigned group
     */
    fun initialLoad() {
        loadGroups()
        loadServers()
    }

    /**
     * Loads the server groups stored in redis into memory.
     */
    private fun loadGroups() {
        Rift.instance.runRedisCommand { redis ->
            for (groupId in redis.smembers("Rift:ServerGroups")) {
                fetchGroupById(groupId).ifPresent { group ->
                    groups[group.id.toLowerCase()] = group
                }
            }
        }
    }

    /**
     * Gets a copy of all the server groups.
     */
    fun getGroups(): List<ServerGroup> {
        return groups.values.toList()
    }

    /**
     * Gets a [ServerGroup] object by the given [groupId].
     */
    fun getGroupById(groupId: String): Optional<ServerGroup> {
        return Optional.ofNullable(getGroups().firstOrNull { group -> group.displayName == groupId })
    }

    /**
     * Attempts to fetch a [ServerGroup] from redis.
     */
    fun fetchGroupById(groupId: String): Optional<ServerGroup> {
        return Optional.ofNullable(Rift.instance.runRedisCommand { client ->
            val map = client.hgetAll("Rift:ServerGroup:$groupId")
            if (map.isEmpty()) {
                null
            } else {
                ServerGroup(map)
            }
        })
    }

    /**
     * Saves the given [group] to redis.
     */
    fun saveGroup(group: ServerGroup) {
        Rift.instance.runRedisCommand { redis ->
            redis.sadd("Rift:ServerGroups", group.id)
            redis.hmset("Rift:ServerGroup:${group.displayName}", group.toMap())
        }

        Rift.instance.pidgin.sendMessage(Message(SERVER_GROUP_UPDATE, group.toMap()))
    }

    /**
     * Loads an existing [ServerGroup] with the given [groupId], or creates a new [ServerGroup] object.
     */
    fun loadOrCreateGroup(groupId: String): ServerGroup {
        return Rift.instance.runRedisCommand { redis ->
            val exists = redis.exists("Rift:ServerGroup:$groupId")

            val group = if (exists) {
                ServerGroup(redis.hgetAll("Rift:ServerGroup:$groupId"))
            } else {
                ServerGroup(groupId)
            }

            if (!exists) {
                saveGroup(group)

                Rift.instance.pidgin.sendMessage(Message(SERVER_GROUP_UPDATE, group.toMap()))
            }

            groups[groupId] = group

            return@runRedisCommand group
        }
    }

    /**
     * Loads the servers stored in redis into memory.
     */
    private fun loadServers() {
        Rift.instance.runRedisCommand { redis ->
            for (serverId in redis.smembers("Rift:Servers")) {
                fetchServerById(serverId).ifPresent { server ->
                    val group = getGroupById(server.group)
                    if (group.isPresent) {
                        group.get().servers.add(server)
                    } else {
                        loadOrCreateGroup(server.group).servers.add(server)
                    }
                }
            }
        }
    }

    /**
     * Gets the [Server]s loaded into memory.
     */
    fun getServers(): List<Server> {
        return groups.values.flatMap { it.servers }
    }

    /**
     * Gets a [Server] by the given [serverName] if loaded into memory.
     */
    fun getServerById(serverName: String, ignoreCase: Boolean = true): Optional<Server> {
        return Optional.ofNullable(getServers().firstOrNull { server -> server.displayName.equals(serverName, ignoreCase = ignoreCase) })
    }

    /**
     * Attempts to fetch a [Server] from redis.
     */
    fun fetchServerById(serverId: String): Optional<Server> {
        return Optional.ofNullable(Rift.instance.runRedisCommand { client ->
            val map = client.hgetAll("Rift:Server:$serverId")
            if (map.isEmpty()) {
                null
            } else {
                Server(map)
            }
        })
    }

    /**
     * Saves the given [server] to redis.
     */
    fun saveServer(server: Server) {
        Rift.instance.runRedisCommand { redis ->
            redis.sadd("Rift:Servers", server.id)
            redis.hmset("Rift:Server:${server.id}", server.toMap())
            redis.hmset("Rift:ServerPorts", mapOf(server.port.toString() to server.id))
        }

        Rift.instance.pidgin.sendMessage(Message(SERVER_UPDATE, server.toMap()))
    }

    /**
     * Deletes the given [server] from redis.
     */
    fun deleteServer(server: Server) {
        Rift.instance.runRedisCommand { redis ->
            redis.srem("Rift:Servers", server.id)
            redis.del("Rift:Server:${server.id}")
            redis.hdel("Rift:ServerPorts", server.port.toString())
        }
    }

    /**
     * Loads an existing [Server] with the given [id], or creates a new [Server] object.
     */
    fun loadOrCreateServer(id: String, port: Int): Server {
        return Rift.instance.runRedisCommand { redis ->
            val exists = redis.exists("Rift:Server:$id")

            val server = if (exists) {
                Server(redis.hgetAll("Rift:Server:$id"))
            } else {
                Server(id, "default", port)
            }

            if (!exists) {
                saveServer(server)

                Rift.instance.pidgin.sendMessage(Message(SERVER_UPDATE, server.toMap()))
            }

            val group = getGroupById(server.group)

            if (group.isPresent) {
                group.get().servers.add(server)
            } else {
                loadOrCreateGroup(server.group).servers.add(server)
            }

            return@runRedisCommand server
        }
    }

    fun getTotalPlayerCount(): Int {
        return groups.values.sumBy { it.getAllServersPlayerCount() }
    }

    fun getOnlineServerCount(): Int {
        return groups.values.sumBy { it.getOnlineServersPlayerCount() }
    }

}
