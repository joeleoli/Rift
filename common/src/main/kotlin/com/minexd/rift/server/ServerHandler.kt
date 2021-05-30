package com.minexd.rift.server

import com.minexd.rift.Rift
import net.evilblock.pidgin.message.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ServerHandler {

    const val GROUP_UPDATE = "ServerGroupUpdate"
    const val GROUP_DELETE = "ServerGroupDelete"

    const val SERVER_UPDATE = "ServerUpdate"
    const val SERVER_UPDATE_COUNT = "ServerUpdateCount"
    const val SERVER_DELETE = "ServerDelete"

    val groups: MutableMap<String, ServerGroup> = ConcurrentHashMap()
    val servers: MutableMap<String, Server> = ConcurrentHashMap()

    fun initialLoad() {
        loadGroups()
        loadServers()
    }

    private fun loadGroups() {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            for (groupId in redis.smembers("Rift:ServerGroups")) {
                val group = fetchGroupById(groupId) ?: continue
                groups[group.id.toLowerCase()] = group
            }
        }
    }

    /**
     * Gets a copy of all the server groups.
     */
    fun getGroups(): Collection<ServerGroup> {
        return groups.values
    }

    /**
     * Gets a [ServerGroup] object by the given [groupId].
     */
    fun getGroupById(groupId: String): ServerGroup? {
        return groups[groupId.toLowerCase()] ?: groups.values.firstOrNull { group -> group.displayName == groupId }
    }

    /**
     * Fetches a [ServerGroup] for the given [groupId].
     */
    fun fetchGroupById(groupId: String): ServerGroup? {
        return Rift.instance.plugin.getRedis().runRedisCommand { client ->
            val map = client.hgetAll("Rift:ServerGroup:$groupId")
            if (map.isEmpty()) {
                null
            } else {
                ServerGroup(map)
            }
        }
    }

    /**
     * Saves the given [group] to redis.
     */
    fun saveGroup(group: ServerGroup) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.sadd("Rift:ServerGroups", group.id)
            redis.hmset("Rift:ServerGroup:${group.displayName}", group.toMap())
        }

        Rift.instance.mainChannel.sendMessage(Message(GROUP_UPDATE, group.toMap()))
    }

    /**
     * Deletes the given [group] from redis.
     */
    fun deleteGroup(group: ServerGroup) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.del("Rift:ServerGroups")
            redis.del("Rift:ServerGroup:${group.displayName}")
        }

        Rift.instance.mainChannel.sendMessage(Message(GROUP_DELETE, mapOf("Group" to group.id)))
    }

    /**
     * Loads an existing [ServerGroup] with the given [groupId], or creates a new [ServerGroup] object.
     */
    fun loadOrCreateGroup(groupId: String): ServerGroup {
        return Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            val exists = redis.exists("Rift:ServerGroup:$groupId")

            val group = if (exists) {
                ServerGroup(redis.hgetAll("Rift:ServerGroup:$groupId"))
            } else {
                ServerGroup(groupId)
            }

            if (!exists) {
                saveGroup(group)

                Rift.instance.mainChannel.sendMessage(Message(GROUP_UPDATE, group.toMap()))
            }

            groups[groupId.toLowerCase()] = group

            return@runRedisCommand group
        }
    }

    /**
     * Loads the servers stored in redis into memory.
     */
    private fun loadServers() {
        try {
            Rift.instance.plugin.getRedis().runRedisCommand { redis ->
                for (serverId in redis.smembers("Rift:Servers")) {
                    val server = fetchServerById(serverId) ?: continue
                    servers[serverId.toLowerCase()] = server

                    val group = loadOrCreateGroup(server.group)
                    if (!group.servers.contains(server)) {
                        group.servers.add(server)
                    }
                }
            }
        } catch (e: Exception) {
            Rift.instance.plugin.getLogger().severe("Failed to load servers!")
            e.printStackTrace()
        }
    }

    /**
     * Gets the [Server]s loaded into memory.
     */
    fun getServers(): Collection<Server> {
        return servers.values
    }

    /**
     * Gets a [Server] for the given [serverName] if loaded into memory.
     */
    fun getServerById(serverName: String, ignoreCase: Boolean = true): Server? {
        return servers[serverName.toLowerCase()] ?: servers.values.firstOrNull { it.displayName.equals(serverName, ignoreCase) }
    }

    /**
     * Attempts to fetch a [Server] from redis.
     */
    fun fetchServerById(serverId: String): Server? {
        return Rift.instance.plugin.getRedis().runRedisCommand { client ->
            val map = client.hgetAll("Rift:Server:$serverId")
            if (map.isEmpty()) {
                null
            } else {
                Server(map)
            }
        }
    }

    /**
     * Saves the given [server] to redis.
     */
    fun saveServer(server: Server) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.sadd("Rift:Servers", server.id)
            redis.hmset("Rift:Server:${server.id}", server.toMap())
            redis.hmset("Rift:ServerPorts", mapOf(server.port.toString() to server.id))
        }

        Rift.instance.mainChannel.sendMessage(Message(SERVER_UPDATE, mapOf("Server" to server.id, "ServerPort" to server.port)))
    }

    /**
     * Deletes the given [server] from redis.
     */
    fun deleteServer(server: Server) {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.srem("Rift:Servers", server.id)
            redis.del("Rift:Server:${server.id}")
            redis.hdel("Rift:ServerPorts", server.port.toString())
        }

        Rift.instance.mainChannel.sendMessage(Message(SERVER_DELETE, mapOf("Server" to server.id)))
    }

    /**
     * Loads an existing [Server] with the given [id], or creates a new [Server] object.
     */
    fun loadOrCreateServer(id: String, port: Int): Server {
        return Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            val exists = redis.exists("Rift:Server:$id")

            var server = if (exists) {
                Server(redis.hgetAll("Rift:Server:$id"))
            } else {
                Server(id, "default", port)
            }

            // sync new data with an existing instance rather than creating a new instance
            val existingInstance = getServerById(id)
            if (existingInstance != null) {
                if (existingInstance.group != server.group) {
                    getGroupById(existingInstance.group)?.servers?.remove(server) // remove server from old group
                }

                existingInstance.group = server.group
                existingInstance.port = server.port
                existingInstance.displayName = server.displayName
                existingInstance.slots = server.slots
                existingInstance.whitelisted = server.whitelisted
                existingInstance.onlineMode = server.onlineMode
                existingInstance.proxied = server.proxied
                existingInstance.lastHeartbeat = server.lastHeartbeat
                existingInstance.currentUptime = server.currentUptime
                existingInstance.currentTps = server.currentTps
                existingInstance.playerCount = server.playerCount
                existingInstance.configuration = server.configuration

                server = existingInstance
            }

            if (!exists) {
                saveServer(server)
            }

            servers[server.id.toLowerCase()] = server

            val group = loadOrCreateGroup(server.group)
            if (!group.servers.contains(server)) {
                group.servers.add(server)
            }

            return@runRedisCommand server
        }
    }

    fun getOnlinePlayerCount(): Int {
        return servers.values.sumBy { it.getPlayerCount().orElse(0) }
    }

}
