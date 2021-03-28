package com.minexd.rift.bukkit.spoof.event

import com.minexd.rift.bukkit.spoof.v1_8_R3.FakeEntityPlayer
import net.evilblock.cubed.plugin.PluginEvent

class SpoofPlayerRemoveEvent(val fakePlayer: FakeEntityPlayer) : PluginEvent()