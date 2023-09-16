package com.redfootdev.mobageddon.modifiers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

data class ImmunityOptions(var entityType: EntityType, var immunities: HashSet<DamageCause>)

class MobImmunities(var plugin: Mobageddon) : Listener {
    var options: HashMap<EntityType, ImmunityOptions> = HashMap()

    init {
        loadSpawningConfigurations()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getModifierSections(plugin, "Immunities")
        for (entityType in sections.keys) {
            val immunities = HashSet<DamageCause>()
            for (cause in sections[entityType]!!.getStringList("DamageImmunities")) {
                try {
                    val damageCause = DamageCause.valueOf(cause)
                    immunities.add(damageCause)
                } catch (error: IllegalArgumentException) {
                    plugin.logError("Invalid Damage Cause: " + cause + " for Mob Immunity for Entity: " + entityType.name)
                }
            }
            options[entityType] = ImmunityOptions(entityType, immunities)
        }
    }

    @EventHandler
    fun modifyMonsterAttributes(event: EntityDamageEvent) {
        if (event.entity is Monster) {
            if (!options.keys.contains(event.entityType)) return
            val immunityOptions = options[event.entityType]
            val cause = event.cause
            if (immunityOptions!!.immunities.contains(cause)) {
                event.isCancelled = true
            }
        }
    }
}