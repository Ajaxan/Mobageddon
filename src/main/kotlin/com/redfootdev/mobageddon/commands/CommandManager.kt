package com.redfootdev.mobageddon.commands

import com.redfootdev.mobageddon.commands.subcommands.TestCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


class CommandManager(private val plugin: JavaPlugin) : CommandExecutor {

    private val subcommands: ArrayList<SubCommand> = ArrayList()

    init {
        plugin.getCommand("example")?.setExecutor(this)
        subcommands.add(TestCommand(plugin))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        if (args.isNotEmpty()) {
            for (i in subcommands.indices) {
                if (args[0].equals(subcommands[i].name, ignoreCase = true)) {
                    subcommands[i].perform(sender, args)
                }
            }
        } else if (args.isEmpty()) {
            sender.sendMessage("--------------------------------")
            for (i in subcommands.indices) {
                sender.sendMessage(subcommands[i].syntax + " - " + subcommands[i].description)
            }
            sender.sendMessage("--------------------------------")
        }
        return true
    }

    fun getSubcommands(): ArrayList<SubCommand> {
        return subcommands
    }

}

