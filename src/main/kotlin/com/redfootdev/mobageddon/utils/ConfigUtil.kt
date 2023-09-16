package com.redfootdev.mobageddon.utils

import com.redfootdev.mobageddon.Mobageddon
import org.bukkit.ChatColor.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType

object ConfigUtil {
    private fun getSections(
        plugin: Mobageddon,
        section: String,
        subSection: String
    ): HashMap<EntityType, ConfigurationSection> {
        val sectionsHashMap = HashMap<EntityType, ConfigurationSection>()
        val configuration = plugin.config
        val mainSection = configuration.getConfigurationSection(section)
            ?: throw Exception("Section Not Found while parsing config: $section")
        for (entity in mainSection.getKeys(false)) {
            try {
                val entityType = EntityType.valueOf(entity)
                val entitySection = mainSection.getConfigurationSection(entity)
                    ?: throw Exception("Entity ($entity) Subsection Not Found while parsing config: $section")
                if (!entitySection.getKeys(false).contains(subSection)) continue
                sectionsHashMap[entityType] = entitySection.getConfigurationSection(subSection)
                    ?: throw Exception("Entity's ($entity) Subsection ($subSection) Not Found while parsing config $section")
            } catch (error: Exception) {
                plugin.logError("${RED}${error.message}")
            }
        }
        return sectionsHashMap
    }

    /*fun getPowerSections(plugin: Mobageddon, powerName: String?): HashMap<EntityType, ConfigurationSection?> {
        val sectionsHashMap = HashMap<EntityType, ConfigurationSection?>()
        val configuration = plugin.config
        val powerSection = configuration.getConfigurationSection("Powers")!!
        for (entity in powerSection.getKeys(false)) {
            try {
                val entityType = EntityType.valueOf(entity)
                val entitySection = powerSection.getConfigurationSection(entity)

                // If this entity doesn't have breaker, continue to the next entity
                if (!entitySection!!.getKeys(false).contains(powerName)) continue
                sectionsHashMap[entityType] = entitySection.getConfigurationSection(powerName!!)
            } catch (error: IllegalArgumentException) {
                plugin.logger.info(ChatColor.RED.toString() + "Invalid Entity in Powers Section: " + entity)
                continue
            }
        }
        return sectionsHashMap
    }*/

    fun getPowerSections(plugin: Mobageddon, powerName: String): HashMap<EntityType, ConfigurationSection> {
        return getSections(plugin, "Powers", powerName)
    }

    fun getModifierSections(plugin: Mobageddon, modifierName: String): HashMap<EntityType, ConfigurationSection> {
        return getSections(plugin, "Modifiers", modifierName)
    }
}