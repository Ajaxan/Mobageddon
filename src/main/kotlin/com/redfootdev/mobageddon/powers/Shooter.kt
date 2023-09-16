package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.potion.PotionEffectType

data class ShooterOptions(
    var entityType: EntityType,
    var frequency: Int,
    var chance: Double,
    var isExplosiveArrow: Boolean,
    var isSilverfishArrow: Boolean,
    var potionArrow: List<PotionEffectType>
)

/**
 * Shooter - Occasionally shoots special projectiles at the target
 * @author joshr
 */
class Shooter(var plugin: Mobageddon) : Listener, Runnable {
    var options: HashMap<EntityType, ShooterOptions> = HashMap()
    private var shooters: HashMap<Monster, Int> = HashMap()

    init {
        loadSpawningConfigurations()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Shooter")
        for (entityType in sections.keys) {
            val frequency = sections[entityType]!!.getInt("Frequency")
            val chance = sections[entityType]!!.getDouble("Chance")
            val explosiveArrow = sections[entityType]!!.getBoolean("ExplosiveArrow")
            val silverfishArrow = sections[entityType]!!.getBoolean("SilverfishArrow")
            val potionArrowNames = sections[entityType]!!.getStringList("PotionArrows")
            val potionEffectTypes: MutableList<PotionEffectType> = ArrayList()
            for (potionArrowName in potionArrowNames) {
                val potionEffectType = PotionEffectType.getByName(potionArrowName)
                if (potionEffectType != null) potionEffectTypes.add(potionEffectType) else plugin.logError("Invalid Potion Effect Type: " + potionArrowName + " for Shooter Power for Entity: " + entityType.name)
            }
            options[entityType] =
                ShooterOptions(entityType, frequency, chance, explosiveArrow, silverfishArrow, potionEffectTypes)
        }
    }

    @EventHandler
    fun spawnNaturalBonusSpawns(event: CreatureSpawnEvent) {
        val entity: Entity = event.entity
        if (!plugin.enabledWorlds.contains(entity.world)) return
        if (entity !is Monster) return
        val monster = event.entity as Monster
        val shooterOptions = options[monster.type] ?: return
        val taskNumber = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (!RandUtil.chance(shooterOptions.chance, 100.0)) {
                return@scheduleSyncRepeatingTask
            }
        }, 100, 20)
        shooters[monster] = taskNumber
    }

    override fun run() {
        for (monster in shooters.keys) {
            val shooterOptions = options[monster.type] ?: continue
            if (monster.target != null && monster.target is Player) {
                val frequency = shooters[monster] ?: continue
                if (frequency >= shooterOptions.frequency) {
                    val explosive = shooterOptions.isExplosiveArrow
                    val silverfish = shooterOptions.isSilverfishArrow
                    val potion = shooterOptions.potionArrow.isNotEmpty()
                    val arrowTypes: MutableList<String?> = ArrayList()
                    if (explosive) arrowTypes.add("explosive")
                    if (silverfish) arrowTypes.add("silverfish")
                    if (potion) arrowTypes.add("potion")
                    val arrowType = RandUtil.fromcollection(arrowTypes).orElse(null) ?: return
                    when (arrowType) {
                        "explosive" -> {}
                        "silverfish" -> {}
                        "potion" -> {}
                    }
                } else {
                    shooters[monster] = frequency + 1
                }
            }
        }
    }
}