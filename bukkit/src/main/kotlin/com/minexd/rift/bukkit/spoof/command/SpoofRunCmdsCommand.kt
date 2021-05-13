package com.minexd.rift.bukkit.spoof.command

import com.minexd.rift.bukkit.RiftBukkitPlugin
import net.evilblock.cubed.command.Command
import com.minexd.rift.bukkit.spoof.SpoofHandler
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.math.Chance
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object SpoofRunCmdsCommand {

    @Command(
        names = ["rs run-cmd"],
        description = "Forces GS to run a command",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun runCommand(sender: CommandSender, @Param(name = "command", wildcard = true) command: String) {
        for (fakePlayer in SpoofHandler.getFakePlayers()) {
            fakePlayer.value.bukkitEntity.performCommand(command)
        }
    }

    @Command(
        names = ["rs run-cmds"],
        description = "Forces a rerun of GS commands",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun rerunCommands(sender: CommandSender) {
        val readCommands = RiftBukkitPlugin.instance.readSpoofActions()
        if (readCommands.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}GS config has no commands!")
            return
        }

        for (fakePlayer in SpoofHandler.getFakePlayers()) {
            val toPerform = arrayListOf<String>()
            toPerform.addAll(readCommands.filter { it.second >= 100.0 }.map { it.first })
            toPerform.add(Chance.weightedPick(readCommands) { it.second }.first)

            for (command in toPerform) {
                if (SpoofHandler.DEBUG) {
                    SpoofHandler.debugLog("Executing command $command for ${fakePlayer.value.name}")
                }

                try {
                    fakePlayer.value.bukkitEntity.performCommand(command)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}