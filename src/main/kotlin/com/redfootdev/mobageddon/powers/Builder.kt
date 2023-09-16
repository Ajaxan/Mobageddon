package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs

data class BuilderOptions(var entityType: EntityType, var buildChance: Double, var buildSpeed: Int, var buildBlock: Material)

/**
 * Builder - Builds bridges and pillars to get to a player
 * @author joshr
 */
class Builder(var plugin: Mobageddon) : Runnable {
    var stuckMonsters: HashMap<LivingEntity, Location> = HashMap()
    var builders: HashSet<Monster>? = null
    var options: HashMap<EntityType, BuilderOptions> = HashMap()

    init {
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 70, 20)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Builder")
        for (entityType in sections.keys) {
            val buildChance = sections[entityType]!!.getDouble("BuildChance")
            val buildSpeed = sections[entityType]!!.getInt("BuildSpeed")
            val buildBlock = sections[entityType]!!.getString("BuildBlock")
            val materialBlock = Material.getMaterial(buildBlock!!)
            if (materialBlock != null) {
                options[entityType] = BuilderOptions(entityType, buildChance, buildSpeed, materialBlock)
            } else {
                plugin.logError("Invalid Block Material: " + buildBlock + " for Builder Power for Entity: " + entityType.name)
            }
        }
    }

    override fun run() {
        for (world in plugin.enabledWorlds) {
            for (monster in world.getEntitiesByClass(Monster::class.java)) {
                val builderOptions = options[monster.type] ?: continue
                if (monster.target != null && monster.target is Player) {
                    val monsterX = monster.location.blockX
                    val monsterY = monster.location.blockY
                    val monsterZ = monster.location.blockZ
                    val monsterLoc =
                        Location(monster.world, monsterX.toDouble(), monsterY.toDouble(), monsterZ.toDouble())
                    if (!stuckMonsters.containsKey(monster)) {
                        stuckMonsters[monster] = monsterLoc
                        continue
                    } else {
                        if (stuckMonsters[monster] == monsterLoc) {
                            if (RandUtil.chance(builderOptions.buildChance * 100, 100.0)) {
                                val player = monster.target as Player?
                                val playerLocation = player!!.location
                                val buildBlockMaterial = builderOptions.buildBlock

                                // If monster not at player height or wall in front, jump and build block
                                // Else if stuck but at right height, build across
                                if (playerLocation.blockY > monsterY || getBlockInfront(monster)!!.type != Material.AIR) {
                                    monster.velocity = monster.velocity.add(Vector(0.0, 0.5, 0.0))
                                    var buildBlock = monsterLoc.block
                                    //TODO this is a bad fix for the weird issue where block spawn height seems to be one off for the first block
                                    if (buildBlock.type == Material.AIR || buildBlock.type == Material.SNOW || buildBlock.type == Material.SAND) {
                                        buildBlock.type = buildBlockMaterial!!
                                        plugin.coAPI!!.logPlacement(
                                            "bloodmoonmob",
                                            buildBlock.location,
                                            buildBlock.type,
                                            buildBlock.blockData
                                        )
                                    } else {
                                        buildBlock = buildBlock.getRelative(BlockFace.UP)
                                        buildBlock.type = buildBlockMaterial!!
                                        plugin.coAPI!!.logPlacement(
                                            "bloodmoonmob",
                                            buildBlock.location,
                                            buildBlock.type,
                                            buildBlock.blockData
                                        )
                                    }
                                } else {

                                    // We get distance to build in the most direct route possible
                                    val distanceX = abs(playerLocation.blockX - monsterX)
                                    val distanceZ = abs(playerLocation.blockZ - monsterZ)
                                    var newBlockFace = BlockFace.EAST

                                    // If further away X, build X first
                                    newBlockFace = if (distanceX > distanceZ) {
                                        if (playerLocation.blockX > monsterX) BlockFace.EAST else BlockFace.WEST
                                    } else {
                                        if (playerLocation.blockZ > monsterZ) BlockFace.SOUTH else BlockFace.NORTH
                                    }

                                    // We know which direction to build, build that direction and make the noice and particles
                                    val buildBlock = monsterLoc.block.getRelative(newBlockFace)
                                    buildBlock.world.playSound(buildBlock.location, Sound.BLOCK_STONE_PLACE, 1.5f, 0.5f)
                                    buildBlock.world.spawnParticle(
                                        Particle.BLOCK_CRACK,
                                        buildBlock.location,
                                        5,
                                        buildBlock.blockData
                                    )
                                    buildBlock.type = buildBlockMaterial
                                    plugin.coAPI!!.logPlacement(
                                        "bloodmoonmob",
                                        buildBlock.location,
                                        buildBlock.type,
                                        buildBlock.blockData
                                    )
                                    stuckMonsters.remove(monster)
                                }
                            }
                        } else {
                            stuckMonsters[monster] = monsterLoc
                        }
                    }
                }
            }
        }
    }

    // Get the block infront of the entity.
    private fun getBlockInfront(entity: LivingEntity): Block? {
        val blocks = entity.getLastTwoTargetBlocks(null, 1)
        return if (blocks.size > 1) {
            blocks[1]
        } else null
    }
}