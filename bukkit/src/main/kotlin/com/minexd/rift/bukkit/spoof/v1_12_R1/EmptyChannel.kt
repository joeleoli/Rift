package com.minexd.rift.bukkit.spoof.v1_12_R1

import io.netty.channel.*
import java.net.SocketAddress

class EmptyChannel(parent: Channel?) : AbstractChannel(parent) {

    private val config = DefaultChannelConfig(this)

    override fun config(): ChannelConfig {
        config.isAutoRead = true
        return config
    }

    override fun doBeginRead() {
    }

    override fun doBind(p0: SocketAddress?) {
    }

    override fun doClose() {
    }

    override fun doDisconnect() {
    }

    override fun doWrite(p0: ChannelOutboundBuffer?) {
    }

    override fun isActive(): Boolean {
        return false
    }

    override fun isCompatible(p0: EventLoop?): Boolean {
        return true
    }

    override fun isOpen(): Boolean {
        return false
    }

    override fun localAddress0(): SocketAddress? {
        return null
    }

    override fun metadata(): ChannelMetadata {
        return ChannelMetadata(true)
    }

    override fun newUnsafe(): AbstractUnsafe? {
        return null
    }

    override fun remoteAddress0(): SocketAddress? {
        return null
    }

    override fun write(msg: Any?): ChannelFuture? {
        return null
    }

    override fun write(msg: Any?, promise: ChannelPromise?): ChannelFuture? {
        return null
    }

    override fun writeAndFlush(msg: Any?): ChannelFuture? {
        return null
    }

    override fun writeAndFlush(msg: Any?, promise: ChannelPromise?): ChannelFuture? {
        return null
    }

}