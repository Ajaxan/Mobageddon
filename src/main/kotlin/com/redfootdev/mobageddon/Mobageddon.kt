package com.redfootdev.mobageddon

import com.redfootdev.mobageddon.modifiers.MobAttributes
import com.redfootdev.mobageddon.modifiers.MobImmunities
import com.redfootdev.mobageddon.powers.Breaker
import com.redfootdev.mobageddon.powers.Builder
import com.redfootdev.mobageddon.powers.Exploder
import com.redfootdev.mobageddon.powers.Griefer
import com.redfootdev.mobageddon.powers.Jumper
import com.redfootdev.mobageddon.powers.Piler
import com.redfootdev.mobageddon.powers.Scouter
import com.redfootdev.mobageddon.powers.Shooter
import com.redfootdev.mobageddon.powers.Smeller
import com.redfootdev.mobageddon.spawning.BonusSpawnHandler
import com.redfootdev.mobageddon.spawning.NaturalSpawnHandler

import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import org.bukkit.ChatColor.*
import org.bukkit.World
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.plugin.java.JavaPlugin


class Mobageddon : JavaPlugin() {
    var coAPI: CoreProtectAPI? = initializeCoreProtectApi()
    var date: Long = 0
    var enabledWorlds: ArrayList<World> = ArrayList()
    private var enableSpawning = false
    private var enableSpreading = false
    private var enableModifiers = false
    private var enablePowers = false
    private val console: ConsoleCommandSender = server.consoleSender

    override fun onEnable() {
        saveDefaultConfig()

        console.sendMessage("${GOLD}✦✴✸ ${RED}Welcome to Mobageddon ${GOLD}✸✴✦")
        console.sendMessage("${RED}[$name] ${GOLD}---------------------------------")
        console.sendMessage("${RED}[$name] ${GOLD}Plugin has been enabled")
        console.sendMessage("${RED}[$name] ${GOLD}---------------------------------")
        console.sendMessage("${RED}[$name] ${GOLD}Version: ${this.description.version}")
        console.sendMessage("${RED}[$name] ${GOLD}---------------------------------")

        initializeConfig()
        initializeAPI()
        initializePowers()
        initializeModifiers()
        initializeSpawning()
    }

    override fun onDisable() {
        console.sendMessage("${RED}[$name] ${GOLD}---------------------------------")
        console.sendMessage("${RED}[$name] ${GOLD}Plugin has been disabled")
        console.sendMessage("${RED}[$name] ${GOLD}---------------------------------")
    }

    private fun initializeConfig() {
        val config = this.config
        enabledWorlds = ArrayList()
        for (worldName in config.getStringList("Worlds")) {
            val world = server.getWorld(worldName)
            if (world == null) {
                logWarning("Invalid World: $worldName")
                continue
            }
            enabledWorlds.add(world)
        }
        enableSpawning = config.getBoolean("EnableSpawning")
        enableSpreading = config.getBoolean("EnableSpreading")
        enableModifiers = config.getBoolean("EnableModifers")
        enablePowers = config.getBoolean("EnablePowers")
    }

    private fun initializeAPI() {
        if (coAPI == null) {
            logWarning("Failed to load CoreProtect API!")
        } else {
            coAPI!!.testAPI()
        }
    }

    private fun initializeModifiers() {
        if (enableModifiers) {
            MobAttributes(this)
            MobImmunities(this)
        }
    }

    private fun initializePowers() {
        if (enablePowers) {
            Breaker(this)
            Builder(this)
            Exploder(this)
            Griefer(this)
            Jumper(this)
            Piler(this)
            Scouter(this)
            Shooter(this)
            Smeller(this)
            //new Striker(this);
        }
    }

    private fun initializeSpawning() {
        if (enableSpawning) {
            NaturalSpawnHandler(this)
            BonusSpawnHandler(this)
        }
        if (enableSpreading) {
            //TODO Re-enable this
            //MobSpreader(this)
        }
    }

    private fun initializeCoreProtectApi(): CoreProtectAPI? {
        val co = server.pluginManager.getPlugin("CoreProtect")

        // Check that CoreProtect is loaded
        if (co == null || co !is CoreProtect) return null

        // Check that the API is enabled
        val coreProtect = co.api
        if (!coreProtect.isEnabled) return null

        // Check that a compatible version of the API is loaded
        return if (coreProtect.APIVersion() < 6) null
        else coreProtect
    }


    fun logInfo(info: String) {
        console.sendMessage("$WHITE[$name] $info")
    }

    fun logWarning(warning: String) {
        console.sendMessage("$YELLOW[$name] $warning")
    }

    fun logError(error: String) {
        console.sendMessage("$RED[$name] $error")
    }
}