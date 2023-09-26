package com.redfootdev.mobageddon.spawning

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster

open class BonusSpawnOptions(open val entityType: EntityType, open val spawnChance: Double, open val extraSpawns: Int)

class BonusSpawnHandler(var plugin: Mobageddon) : Runnable {
    private var enableSpawning = false
    private var enableBonusSpawning = false
    private var maxHostileLimit = 0
    private var playerMaxHostileLimit = 0
    private var bonusSpawnsFrequency = 0
    private var bonusAttemptsPerSpawn = 0
    private var bonusSpawnLightLevel = 0
    private var trueBonusSpawns: LinkedHashMap<EntityType, BonusSpawnOptions> = LinkedHashMap()
    var bonusSpawnsTotalWeight = 0.0

    init {
        plugin.logInfo("Bonus Spawning Enabled")
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 60, 110L)
    }

    private fun loadSpawningConfigurations() {
        val config = plugin.config
        val spawningSection = config.getConfigurationSection("Spawning") ?: throw Exception("Failed to find Spawning Section")
        val bonusSection = spawningSection.getConfigurationSection("Bonus")
        val bonusMobsSection = bonusSection!!.getConfigurationSection("Mobs")
        enableSpawning = config.getBoolean("EnableSpawning")
        enableBonusSpawning = bonusSection.getBoolean("Enabled")
        maxHostileLimit = config.getInt("MaxHostileLimit")
        playerMaxHostileLimit = config.getInt("PlayerMaxHostileLimit")
        bonusSpawnsFrequency = bonusSection.getInt("SpawnsFrequency")
        bonusAttemptsPerSpawn = bonusSection.getInt("AttemptsPerSpawn")
        bonusSpawnLightLevel = bonusSection.getInt("LightLevel")

        // Load the Bonus Mob spawning options
        for (entityTypeName in bonusMobsSection!!.getKeys(false)) {
            try {
                val mobType = EntityType.valueOf(entityTypeName)
                val spawnChance = bonusMobsSection.getDouble("$entityTypeName.SpawnChance")
                val extraSpawns = bonusMobsSection.getInt("$entityTypeName.ExtraSpawns")
                val options = BonusSpawnOptions(mobType, spawnChance, extraSpawns)
                trueBonusSpawns[mobType] = options
            } catch (e: IllegalArgumentException) {
                plugin.logError("Invalid Mob Type in Bonus Spawns: $entityTypeName")
            }
        }
    }


    override fun run() {
        if (!enableSpawning || !enableBonusSpawning) return
        for (world in plugin.enabledWorlds) {
            if (world.getEntitiesByClass(Monster::class.java).size > maxHostileLimit) continue
            for (player in world.players) {
                plugin.logInfo("Player in world: ${player.name}")
                var hostileCount = 0
                for (entityNearby in player.getNearbyEntities(48.0, 48.0, 48.0)) {
                    if (entityNearby is Monster) {
                        if (player == entityNearby.target) {
                            hostileCount++
                        }
                    }
                }
                if (hostileCount >= playerMaxHostileLimit) continue
                val playerLocation = player.location
                for (i in 0 until bonusAttemptsPerSpawn) {

                    // First we create our modifiers to determine the chunk they spawn in
                    var xModifier = RandUtil.range(-5, 5)
                    var zModifier = RandUtil.range(-5, 5)
                    plugin.logInfo("chunk xModifier: ${xModifier}")
                    plugin.logInfo("chunk zModifier: ${zModifier}")

                    // Make sure they don't spawn too close
                    if (xModifier == -1) xModifier = -2
                    if (xModifier == 1 || xModifier == 0) xModifier = 2
                    if (zModifier == -1) zModifier = -2
                    if (zModifier == 1 || zModifier == 0) zModifier = 2

                    // Get the new location
                    val spawnX = xModifier * 16 + playerLocation.blockX + RandUtil.range(-8, 8)
                    val spawnZ = zModifier * 16 + playerLocation.blockZ + RandUtil.range(-8, 8)
                    val spawnY = world.getHighestBlockYAt(spawnX, spawnZ)
                    val spawnLocation = Location(world, spawnX.toDouble(), spawnY.toDouble(), spawnZ.toDouble())

                    // Check the light level
                    if (spawnLocation.block.lightLevel > bonusSpawnLightLevel) continue

                    // Choose Entity Type and check spawn chance
                    val selectedEntity = getRandomWeightedEntity()

                    // spawn entites by adding one to the extra spawns
                    val extraSpawns = trueBonusSpawns[selectedEntity]?.extraSpawns ?: throw Exception("No extra spawns found for $selectedEntity")
                    spawnGroup(spawnLocation, selectedEntity, extraSpawns + 1)
                }
            }
        }
    }


    private fun getRandomWeightedEntity(): EntityType {
        var totalWeight = 0.0
        val entityTypes = ArrayList<EntityType>()
        trueBonusSpawns.forEach { (entityType, options) ->
            totalWeight += options.spawnChance
            entityTypes.add(entityType)
        }

        // Now choose a random item.
        var idx = 0
        var remainingWeight = Math.random() * totalWeight
        while (idx < entityTypes.size - 1) {
            remainingWeight -= trueBonusSpawns[entityTypes[idx]]?.spawnChance ?: throw Exception("Error choosing weighted entity")
            if (remainingWeight <= 0.0) break
            ++idx
        }
        return entityTypes[idx]
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
            world.spawnEntity(spawnLocation, entityType)
            plugin.logInfo("Spawned entity at: ${spawnLocation.toString()}")
        }
    }
}