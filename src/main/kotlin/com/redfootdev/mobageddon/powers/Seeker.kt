package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.GameMode
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player

data class SmellerOptions(
    var entityType: EntityType,
    var rawMeetRange: Int,
    var cookedMeetRaange: Int,
    var inherentRange: Int
)

/**
 * Smeller - Can sense all players within a given distance and attack them
 * @author joshr
 */
class Smeller(var plugin: Mobageddon) : Runnable {
    var options: HashMap<EntityType, SmellerOptions>

    init {
        options = HashMap()
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 70, 115L)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Smeller")
        for (entityType in sections.keys) {
            val rawMeetRange = sections[entityType]!!.getInt("RawMeetRange")
            val cookedMeetRange = sections[entityType]!!.getInt("CookedMeetRange")
            val inherentRange = sections[entityType]!!.getInt("InherentRange")
            options[entityType] = SmellerOptions(entityType, rawMeetRange, cookedMeetRange, inherentRange)
        }
    }

    override fun run() {
        for (world in plugin.enabledWorlds) {
            for (monster in world.getEntitiesByClass(Monster::class.java)) {
                val seekerOptions = options[monster.type] ?: continue
                if (monster.target == null) {
                    val baseDistance = seekerOptions.inherentRange
                    val targetPlayer = monster.getNearbyEntities(
                        baseDistance.toDouble(),
                        baseDistance.toDouble(),
                        baseDistance.toDouble()
                    )
                            .stream()
                            .filter { mob: Entity? -> mob is Player }
                            .map { mob -> mob as Player }
                            .findFirst()
                    if (targetPlayer.isPresent && targetPlayer.get().gameMode != GameMode.CREATIVE && targetPlayer.get().gameMode != GameMode.SPECTATOR) {
                        monster.target = targetPlayer.get()
                    }
                }
            }
        }
    }
}