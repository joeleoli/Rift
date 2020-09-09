package net.evilblock.rift

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.evilblock.rift.server.ServerMessages
import net.evilblock.rift.server.ServerHandler
import net.evilblock.pidgin.Pidgin
import net.evilblock.pidgin.PidginOptions
import net.evilblock.rift.plugin.Plugin
import net.evilblock.rift.queue.QueueHandler
import net.evilblock.rift.queue.QueueMessages
import redis.clients.jedis.Jedis

class Rift(val plugin: Plugin) {

    companion object {
        @JvmStatic
        lateinit var instance: Rift

        @JvmStatic
        val GSON: Gson = GsonBuilder().create()
    }

    lateinit var pidgin: Pidgin

    init {
        instance = this
    }

    fun initialLoad() {
        instance = this

        pidgin = Pidgin("Rift", plugin.getJedisPool(), PidginOptions(async = true))

        ServerHandler.initialLoad()
        QueueHandler.initialLoad()

        pidgin.registerListener(ServerMessages)
        pidgin.registerListener(QueueMessages)
    }

    /**
     * A functional method for using a pooled [Jedis] resource and returning data.
     *
     * @param lambda the function
     */
    fun <T> runRedisCommand(lambda: (Jedis) -> T): T {
        if (plugin.getJedisPool().isClosed) {
            throw IllegalStateException("A connection to the redis server couldn't be established or has been forcefully closed")
        }

        try {
            plugin.getJedisPool().resource.use { redis -> return lambda(redis) }
        } catch (e: Exception) {
            throw RuntimeException("Could not use resource and return", e)
        }
    }

}