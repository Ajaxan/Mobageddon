package com.redfootdev.mobageddon.commands.subcommands



import com.redfootdev.mobageddon.commands.SubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * /example test
 */
class TestCommand(private val plugin: JavaPlugin) : SubCommand() {
    override val name = "test"
    override val description = "Example test command to make sure commands are working"
    override val syntax = "/example test"


    override fun perform(sender: CommandSender, args: Array<String>) {
        if (sender is Player) sendError(sender,"Players see an error example test")
        else sendInfo(sender, "You have run the example test command")
    }

}