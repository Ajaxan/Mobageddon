package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import java.util.*
import kotlin.collections.HashMap

data class ExploderOptions(
    var entityType: EntityType,
    var explosionSize: Int,
    var isExplosionFire: Boolean,
    var respawnChance: Double,
    var respawnType: EntityType
)

/**
 * Exploder - Explodes on death with a specifided level of damage as well as resummon chance
 * @author joshr
 */
class Exploder(var plugin: Mobageddon) {
    var options: EnumMap<EntityType, ExploderOptions> = EnumMap(EntityType::class.java)

    init {
        loadSpawningConfigurations()
    }

    private fun loadSpawningConfigurations() {
        //var sections = HashMap<EntityType, ConfigurationSection>()
        val sections = ConfigUtil.getPowerSections(plugin, "Exploder")
        for (entityType in sections.keys) {
            val explosionSize = sections[entityType]!!.getInt("ExplosionSize")
            val explosionFire = sections[entityType]!!.getBoolean("ExplosionFire")
            val respawnChance = sections[entityType]!!.getDouble("RespawnChance")
            val respawnType = sections[entityType]!!.getString("RespawnType")
            try {
                val respawnEntityType = EntityType.valueOf(respawnType!!)
                options[entityType] =
                    ExploderOptions(entityType, explosionSize, explosionFire, respawnChance, respawnEntityType)
            } catch (error: IllegalArgumentException) {
                plugin.logError("Invalid Respawn Entity Type: " + respawnType + " for Builder Power for Entity: " + entityType.name)
            }
        }
    }
}