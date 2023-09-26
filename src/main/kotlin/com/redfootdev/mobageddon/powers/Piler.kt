package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.*

data class PilerOptions(
    var entityType: EntityType,
    var fleshBlock: Material,
    var nearbyMobsNeeded: Int,
    var pileChance: Double,
    var isSacrfice: Boolean
)

/**
 * Piler - Will sacrifice itself (or not) to create a flesh block to aid other in reaching the target
 * @author joshr
 */
class Piler(var plugin: Mobageddon) : Runnable {
    var stuckMonsters: HashMap<Monster, Location>
    var options: HashMap<EntityType, PilerOptions>

    init {
        stuckMonsters = HashMap()
        options = HashMap()
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 30, 20L)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Piler")
        for (entityType in sections.keys) {
            val nearbyMobsNeeded = sections[entityType]!!.getInt("NearbyMobsNeeded")
            val pileChance = sections[entityType]!!.getDouble("PileChance")
            val sacrfice = sections[entityType]!!.getBoolean("Sacrfice")
            val fleshBlock = sections[entityType]!!.getString("FleshBlock")
            val materialBlock = Material.getMaterial(fleshBlock!!)
            if (materialBlock != null) {
                options[entityType] = PilerOptions(entityType, materialBlock, nearbyMobsNeeded, pileChance, sacrfice)
            } else {
                plugin.logError("Invalid Flesh Block Material: " + fleshBlock + " for Piler Power for Entity: " + entityType.name)
            }
        }
    }

    // ------- Preserved for Posterity, Jake and I had a conversation over eclipse code together
    // Did that work????
    // yea, this is kinda sick
    //Hot damn
    // I am a vscode kinda guy, i might downlod the extension
    // Yeah I think so. This is definitely built to work with VC as well
    // Haha I love VSCode but years of minecraft + eclipse means I can't do it any other way
    // only reason i use vscode is cuz it has a really good emacs bindings extension
    // Fair enough. Beyond that is just has good extensions for all web dev stuff
    // anyway... nice CODE jesus
    // Haha yeah I'm about to go get dinner, but hey maybe sometime later we could actually code togetrer
    // not sure one what, but we can!
    //epic, ima go work on my cs assignment, but this is cool
    // later
    // ----------------------------------------------------------------------------------------------------

    override fun run() {
        for (world in plugin.enabledWorlds) {
            for (monster in world.getEntitiesByClass(Monster::class.java)) {
                val pilerOptions = options[monster.type] ?: continue
                if (monster.target != null && monster.target is Player) {
                    // If the block in front is air we can't pile. Wouldn't make sense
//					if(getBlockInfront(monster) == null || getBlockInfront(monster).getType().equals(Material.AIR)) {
//						continue;
//					}
                    val monsterX = monster.location.blockX
                    val monsterY = monster.location.blockY
                    val monsterZ = monster.location.blockZ
                    val monsterLoc =
                        Location(monster.world, monsterX.toDouble(), monsterY.toDouble(), monsterZ.toDouble())

                    // Find mobs that are stationary
                    if (!stuckMonsters.containsKey(monster)) {
                        stuckMonsters[monster] = monsterLoc
                        continue
                    } else {
                        if (stuckMonsters[monster] == monsterLoc) {
                            // MAke sure they are surrounded by other mobs so they can pile
                            if (monster.getNearbyEntities(2.0, 2.0, 2.0).stream()
                                    .filter { entity: Entity? -> entity is Monster }
                                    .count() > pilerOptions.nearbyMobsNeeded
                            ) {
                                if (RandUtil.chance(pilerOptions.pileChance * 100, 100.0)) {
                                    val buildBlockMaterial = pilerOptions.fleshBlock
                                    var buildBlock = monsterLoc.block
                                    if (buildBlock.type == Material.AIR || buildBlock.type == Material.SNOW) {
                                        buildBlock.type = buildBlockMaterial
                                        plugin.coAPI!!.logPlacement(
                                            "mobageddon",
                                            buildBlock.location,
                                            buildBlock.type,
                                            buildBlock.blockData
                                        )
                                    } else {
                                        buildBlock = buildBlock.getRelative(BlockFace.UP)
                                        buildBlock.type = buildBlockMaterial
                                        plugin.coAPI!!.logPlacement(
                                            "mobageddon",
                                            buildBlock.location,
                                            buildBlock.type,
                                            buildBlock.blockData
                                        )
                                    }
                                    stuckMonsters.remove(monster)
                                    if (pilerOptions.isSacrfice) {
                                        monster.damage(999.0)
                                    }
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