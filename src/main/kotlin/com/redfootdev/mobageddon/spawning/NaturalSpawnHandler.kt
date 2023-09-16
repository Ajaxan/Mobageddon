package com.redfootdev.mobageddon.spawning

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent


data class NaturalSpawnOptions(
    override val entityType: EntityType,
    override val spawnChance: Double,
    override val extraSpawns: Int,
    val shouldReplaceSpawn: Boolean,
    var replacementEntityType: EntityType
) : BonusSpawnOptions(entityType, spawnChance, extraSpawns)

/*
 * There are three types of bonus spawns that can be implemented
 * 1. Natural Bonus spawns. Simply spawns a mob close by to naturally spawning mobs
 * 2. True Bonus Spawns. A separate spawning algorithm that acts as a mirror in behavior to natural spawns.
 * 3. Point Bonus Spawns. Spawn points where mobs will spawn from. <NOT IMPLEMENTED>
 */
class NaturalSpawnHandler(var plugin: Mobageddon) : Listener {
    private var enableSpawning = false
    private var enableBonusSpawning = false
    private var enableNaturalSpawning = false
    private var maxHostileLimit = 0
    private var playerMaxHostileLimit = 0
    private var bonusSpawnsFrequency = 0
    private var bonusAttemptsPerSpawn = 0
    private var bonusSpawnLightLevel = 0
    private var trueBonusSpawns: LinkedHashMap<EntityType, BonusSpawnOptions> = LinkedHashMap()
    private var naturalBonusSpawns: HashMap<EntityType, NaturalSpawnOptions> = HashMap()
    private var spawnedMonsters: MutableList<Entity> = ArrayList()
    var bonusSpawnsTotalWeight = 0.0

    init {
        loadSpawningConfigurations()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun loadSpawningConfigurations() {
        val config = plugin.config
        val spawningSection = config.getConfigurationSection("Spawning") ?: throw Exception("Failed to find Spawning Section")
        val naturalSection = spawningSection.getConfigurationSection("Natural")
        val naturalMobsSection = naturalSection!!.getConfigurationSection("Mobs")
        enableSpawning = config.getBoolean("EnableSpawning")
        enableNaturalSpawning = naturalSection.getBoolean("Enabled")

        // Load the Natural Mob spawning options
        for (entityTypeName in naturalMobsSection!!.getKeys(false)) {
            try {
                val mobType = EntityType.valueOf(entityTypeName)
                val spawnChance = naturalMobsSection.getDouble("$entityTypeName.SpawnChance")
                val extraSpawns = naturalMobsSection.getInt("$entityTypeName.ExtraSpawns")
                val replaceSpawn = naturalMobsSection.getBoolean("$entityTypeName.ReplaceSpawn")
                val replacementEntityType =
                    EntityType.valueOf(naturalMobsSection.getString("$entityTypeName.ReplaceType")!!)
                val options =
                    NaturalSpawnOptions(mobType, spawnChance, extraSpawns, replaceSpawn, replacementEntityType)
                naturalBonusSpawns[mobType] = options
            } catch (e: IllegalArgumentException) {
                plugin.logError("Invalid Mob Type in Natural Spawns: $entityTypeName")
            }
        }
    }

    /**
     * spawnNaturalBonusSpawns - Modifies natural spawns
     * @param event
     */
    @EventHandler
    fun spawnNaturalBonusSpawns(event: CreatureSpawnEvent) {
        val entity: Entity = event.entity
        val entityType = event.entityType
        val eventWorld = event.location.world

        // If not an enabled world return
        // If the entity isn't a monster return
        // If the mob spawned through a plugin return (prevents spawn stacking)
        if (!enableSpawning || !enableNaturalSpawning) return
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL || event.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER) return
        if (plugin.enabledWorlds.contains(eventWorld)) return
        if (entity !is Monster) return
        if (spawnedMonsters.contains(entity)) {
            spawnedMonsters.remove(entity)
            return
        }
        val spawnOptions = naturalBonusSpawns[entityType] ?: return

        // If we fail to spawn, cancel all spawns and return
        if (!RandUtil.chance(spawnOptions.spawnChance, 100.0)) {
            event.isCancelled = true
            return
        }

        // If we are replacing the entity type for this spawning, set the new entity type
        var spawnEntityType: EntityType = entityType
        if (spawnOptions.shouldReplaceSpawn) {
            spawnEntityType = spawnOptions.replacementEntityType
            spawnedMonsters.add(eventWorld.spawnEntity(event.location, spawnEntityType, ))
            event.isCancelled = true
        }

        // Spawn extra entities if required
        spawnGroup(entity.getLocation(), spawnEntityType, spawnOptions.extraSpawns)
    }

    private fun spawnGroup(location: Location, entityType: EntityType, groupSize: Int) {
        val world = location.world
        for (i in 0 until groupSize) {
            val spawnX = location.blockX + RandUtil.range(-3, 3)
            val spawnZ = location.blockZ + RandUtil.range(-3, 3)
            val spawnY = location.world.getHighestBlockYAt(spawnX, spawnZ)
            val spawnLocation = Location(world, spawnX.toDouble(), spawnY.toDouble(), spawnZ.toDouble())
            when (entityType) {
                EntityType.GHAST, EntityType.PHANTOM, EntityType.BLAZE, EntityType.VEX -> spawnLocation.add(
                    0.0,
                    5.0,
                    0.0
                )
                else -> {}
            }
            spawnedMonsters.add(world.spawnEntity(spawnLocation, entityType))
        }
    }



}