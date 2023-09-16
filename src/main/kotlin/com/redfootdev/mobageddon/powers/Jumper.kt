package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class JumperOptions(var entityType: EntityType, var jumpPower: Double)

/**
 * Jumper - Will jump at a player when they can move no further
 * @author joshr
 */
class Jumper(var plugin: Mobageddon) : Runnable {
    var jumperMap: HashMap<LivingEntity, Location> = HashMap()
    var options: HashMap<EntityType, JumperOptions> = HashMap()

    init {
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 70, 50L)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Jumper")
        for (entityType in sections.keys) {
            val jumpPower = sections[entityType]!!.getDouble("JumpPower")
            options[entityType] = JumperOptions(entityType, jumpPower)
        }
    }

    override fun run() {
        for (world in plugin.enabledWorlds) {
            for (monster in world.getEntitiesByClass(Monster::class.java)) {
                val jumperOptions = options[monster.type] ?: continue
                if (monster.target != null && monster.target is Player) {
                    val target = monster.target as Player
                    if (getBlockInfront(monster) == null || getBlockInfront(monster)!!.type != Material.AIR) {
                        return
                    }
                    val x = monster.location.blockX
                    val y = monster.location.blockY
                    val z = monster.location.blockZ
                    if (!jumperMap.containsKey(monster)) {
                        jumperMap[monster] = Location(monster.world, x.toDouble(), y.toDouble(), z.toDouble())
                        return
                    } else {
                        //int ox = jumperMap.get(monster).getBlockX();
                        //int oy = jumperMap.get(monster).getBlockY();
                        //int oz = jumperMap.get(monster).getBlockZ();
                        if (jumperMap[monster] == Location(monster.world, x.toDouble(), y.toDouble(), z.toDouble())) {
                            if (RandUtil.chance(3, 3)) {
                                faceDirection(monster, target.location)
                                val unitVector = Vector(monster.location.direction.x, 0.0, monster.location.direction.z)
                                //unitVector = unitVector.normalize(); 
                                val jumpPower = jumperOptions.jumpPower
                                unitVector.add(Vector(0.0, 1 * jumpPower, 0.0))
                                monster.velocity = unitVector
                            }
                        } else {
                            jumperMap[monster] = Location(monster.world, x.toDouble(), y.toDouble(), z.toDouble())
                        }
                    }
                }
            }
        }
    }

    private fun getBlockInfront(entity: LivingEntity): Block? {
        val blocks = entity.getLastTwoTargetBlocks(null, 1)
        return if (blocks.size > 1) {
            blocks[1]
        } else null
    }

    fun faceDirection(le: LivingEntity, target: Location) {
        val dir = target.clone().subtract(le.eyeLocation).toVector()
        val loc = le.location.setDirection(dir)
        le.teleport(loc)
    }
}