package com.redfootdev.mobageddon.commands

import org.bukkit.ChatColor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

open abstract class NamedCommandExecutor: CommandExecutor {
    abstract val name: String

    fun sendError(sender: CommandSender, message: String): Boolean {
        sender.sendMessage("${ChatColor.RED}${message}")
        return true
    }

    fun sendInfo(sender: CommandSender, message: String): Boolean {
        sender.sendMessage("${ChatColor.GREEN}${message}")
        return true
    }

    fun sendWarn(sender: CommandSender, message: String): Boolean {
        sender.sendMessage("${ChatColor.YELLOW}${message}")
        return true
    }
}