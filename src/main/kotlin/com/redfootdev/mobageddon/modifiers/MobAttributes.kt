package com.redfootdev.mobageddon.modifiers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

data class AttributeOptions(var entityType: EntityType, var attributes: HashMap<Attribute, Double>)

class MobAttributes(var plugin: Mobageddon) : Listener {
    var options: HashMap<EntityType, AttributeOptions> = HashMap()

    companion object {
        var MAX_HEALTH = "minecraft:generic.max_health"
        var FOLLOW_RANGE = "minecraft:generic.follow_range"
        var KNOCKBACK_RESISTANCE = "minecraft:generic.knockback_resistance"
        var MOVEMENT_SPEED = "minecraft:generic.movement_speed"
        var ATTACK_DAMAGE = "minecraft:generic.attack_damage"
        var ARMOR = "minecraft:generic.armor"
        var ARMOR_TOUGHNESS = "minecraft:generic.armor_toughness"
        var ATTACK_KNOCKBACK = "minecraft:generic.attack_knockback"
    }

    init {
        loadSpawningConfigurations()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getModifierSections(plugin, "Attributes")
        for (entityType in sections.keys) {
            val entitySection = sections[entityType]
            val attributes = HashMap<Attribute, Double>()
            for (attributeName in entitySection!!.getKeys(false)) {
                try {
                    val attribute = Attribute.valueOf(attributeName)
                    attributes[attribute] = entitySection.getDouble(attributeName)
                } catch (error: IllegalArgumentException) {
                    plugin.logError("Invalid Attribute: " + attributeName + " for Attribute for Entity: " + entityType.name)
                }
            }
            options[entityType] = AttributeOptions(entityType, attributes)
        }
    }

    @EventHandler
    fun modifyMonsterAttributes(event: EntitySpawnEvent) {
        if (event.entity is Monster) {
            val monster = event.entity as Monster
            val entityType = monster.type
            if (!options.keys.contains(entityType)) return
            val attributeOptions = options[entityType]
            val attributes = attributeOptions!!.attributes
            for (attribute in attributes.keys) {
                monster.getAttribute(attribute)!!.baseValue = attributes[attribute]!!
            }
        }
    }


}