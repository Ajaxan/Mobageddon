package com.redfootdev.mobageddon.commands

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

abstract class SubCommand {
    //name of the subcommand ex. /prank <subcommand> <-- that
    abstract val name: String

    //ex. "This is a subcommand that let's a shark eat someone"
    abstract val description: String

    //How to use command ex. /prank freeze <player>
    abstract val syntax: String

    //code for the subcommand
    abstract fun perform(sender: CommandSender, args: Array<String>)

    fun sendError(sender: CommandSender, message: String) {
        sender.sendMessage("${ChatColor.RED}${message}")
    }

    fun sendInfo(sender: CommandSender, message: String) {
        sender.sendMessage("${ChatColor.GREEN}${message}")
    }

    fun sendWarn(sender: CommandSender, message: String) {
        sender.sendMessage("${ChatColor.YELLOW}${message}")
    }
}
