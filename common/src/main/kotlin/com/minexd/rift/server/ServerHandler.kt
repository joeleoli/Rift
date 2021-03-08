package com.minexd.rift.server

import com.minexd.rift.Rift
import net.evilblock.pidgin.message.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ServerHandler {

    const val GROUP_UPDATE = "ServerGroupUpdate"
    const val SERVER_UPDATE = "ServerUpdate"

    val groups: MutableMap<String, ServerGroup> = ConcurrentHashMap()

    fun initialLoad() {
        loadGroups()
        loadServers()
    }

    private fun loadGroups() {
        Rift.instance.plugin.getRedis().runRedisCommand { redis ->
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
    fun getGroups(): Collection<ServerGroup> {
        return groups.values
    }

    /**
     * Gets a [ServerGroup] object by the given [groupId].
     */
    fun getGroupById(groupId: String): ServerGroup? {
        return getGroups().firstOrNull { group -> group.displayName == groupId }
    }

    /**
     * Fetches a [ServerGroup] for the given [groupId].
     */
    fun fetchGroupById(groupId: String): Optional<ServerGroup> {
        return Rift.instance.plugin.getRedis().runRedisCommand { client ->
            val map = client.hgetAll("Rift:ServerGroup:$groupId")
            if (map.isEmpty()) {
                Optional.empty()
            } else {
                Optional.of(ServerGroup(map))
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

        Rift.instance.pidgin.sendMessage(Message(GROUP_UPDATE, group.toMap()))
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

                Rift.instance.pidgin.sendMessage(Message(GROUP_UPDATE, group.toMap()))
            }

            groups[groupId] = group

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
                    fetchServerById(serverId).ifPresent { server ->
                        val group = loadOrCreateGroup(server.group)
                        if (!group.servers.contains(server)) {
                            group.servers.add(server)
                        }
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
    fun getServers(): List<Server> {
        return groups.values.flatMap { it.servers }
    }

    /**
     * Gets a [Server] for the given [serverName] if loaded into memory.
     */
    fun getServerById(serverName: String, ignoreCase: Boolean = true): Optional<Server> {
        return Optional.ofNullable(
            getServers()
                .firstOrNull { server -> server.id.equals(serverName, ignoreCase = true)
                || server.displayName.equals(serverName, ignoreCase = ignoreCase) })
    }

    /**
     * Attempts to fetch a [Server] from redis.
     */
    fun fetchServerById(serverId: String): Optional<Server> {
        return Rift.instance.plugin.getRedis().runRedisCommand { client ->
            val map = client.hgetAll("Rift:Server:$serverId")
            if (map.isEmpty()) {
                Optional.empty()
            } else {
                Optional.of(Server(map))
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

        Rift.instance.pidgin.sendMessage(Message(SERVER_UPDATE, mapOf("Server" to server.id, "ServerPort" to server.port)))
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
    }

    /**
     * Loads an existing [Server] with the given [id], or creates a new [Server] object.
     */
    fun loadOrCreateServer(id: String, port: Int): Server {
        return Rift.instance.plugin.getRedis().runRedisCommand { redis ->
            val exists = redis.exists("Rift:Server:$id")

            val server = if (exists) {
                Server(redis.hgetAll("Rift:Server:$id"))
            } else {
                Server(id, "default", port)
            }

            if (!exists) {
                saveServer(server)
            }

            val group = loadOrCreateGroup(server.group)
            if (!group.servers.contains(server)) {
                group.servers.add(server)
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
