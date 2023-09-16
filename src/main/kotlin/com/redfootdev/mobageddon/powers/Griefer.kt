package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType

data class GrieferOptions(
    var entityType: EntityType,
    var griefFrequency: Int,
    var breakRange: Int,
    var isBreakLightSources: Boolean,
    var isBreakGlass: Boolean,
    var isSetFires: Boolean
)

/**
 * Griefer - Breaks nearby light sources, glass, and sets fires
 * @author joshr
 */
class Griefer(var plugin: Mobageddon) {
    var options: HashMap<EntityType, GrieferOptions>

    init {
        options = HashMap()
        loadSpawningConfigurations()
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Griefer")
        for (entityType in sections.keys) {
            val griefFrequency = sections[entityType]!!.getInt("GriefFrequency")
            val breakRange = sections[entityType]!!.getInt("BreakRange")
            val breakLightSources = sections[entityType]!!.getBoolean("BreakLightSources")
            val breakGlass = sections[entityType]!!.getBoolean("BreakGlass")
            val setFires = sections[entityType]!!.getBoolean("SetFires")
            options[entityType] =
                GrieferOptions(entityType, griefFrequency, breakRange, breakLightSources, breakGlass, setFires)
        }
    }
}