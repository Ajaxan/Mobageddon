package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.blocks.HealthBlock
import com.redfootdev.mobageddon.blocks.HealthDefaults
import com.redfootdev.mobageddon.utils.ConfigUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player

data class BreakerOptions(var entityType: EntityType, var damageMultiple: Int)

/**
 * Breaker - Breaks blocks to get to the target
 * @author joshr
 */
class Breaker(var plugin: Mobageddon) : Runnable {
    var blockMap: HashMap<Block, HealthBlock>
    var options: HashMap<EntityType, BreakerOptions>

    init {
        blockMap = HashMap()
        options = HashMap()
        loadSpawningConfigurations()
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 60, 20L)
    }

    private fun loadSpawningConfigurations() {
        var sections = HashMap<EntityType, ConfigurationSection>()
        sections = ConfigUtil.getPowerSections(plugin, "Breaker")
        for (entityType in sections.keys) {
            val damageMultiple = sections[entityType]!!.getInt("DamageMultiple")
            options[entityType] = BreakerOptions(entityType, damageMultiple)
        }
        val hd = HealthDefaults(plugin)
        hd.loadDefaults()
    }

    override fun run() {
        for (world in plugin.enabledWorlds) {
            for (monster in world.getEntitiesByClass(Monster::class.java)) {
                if (!options.containsKey(monster.type)) continue
                val breakerOptions = options[monster.type]
                if (monster.target != null && monster.target is Player) {
                    val blockToBreak = getBlockInfront(monster)
                    if (blockToBreak != null && blockToBreak.type != Material.AIR) {
                        val damage = breakerOptions!!.damageMultiple
                        val hBlock = damageBlock(blockToBreak, damage)
                        if (hBlock != null) {
                            if (hBlock.isBroken()) {
                                hBlock.mobBreakBlock(monster, blockToBreak)
                                plugin.coAPI!!.logRemoval(
                                    "bloodmoonmob",
                                    blockToBreak.location,
                                    blockToBreak.type,
                                    blockToBreak.blockData
                                )
                            } else {
                                hBlock.mobDamageBlock(monster, blockToBreak)
                            }
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

    private fun damageBlock(block: Block, damage: Int): HealthBlock? {
        return if (blockMap.containsKey(block)) {
            blockMap[block]!!.addDamage(damage)
            blockMap[block]
        } else {
            val hBlock = HealthBlock(block)
            if (hBlock.health == -1) {
                return null
            }
            if (!hBlock.addDamage(damage)) {
                blockMap[block] = hBlock
            }
            hBlock
        }
    }
}