package com.redfootdev.mobageddon.blocks

import com.redfootdev.mobageddon.utils.RandUtil.randomInt
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity

class HealthBlock(private var block: Block) {
    private var mat: Material = block.type
    private var loc: Location = block.location
    private var damage: Int = 0
    var health: Int = HealthDefaults.Companion.getMatHealth(mat)
    private var interval: Int = health / 10
    private var blockID: Int = randomInt()

    /*
	 * Adds value of damage to the block.
	 * Returns true if the block has more damage than health
	 * Returns false if the opposite is true
	 */
    fun addDamage(damage: Int): Boolean {
        this.damage += damage
        return isBroken()
    }

    fun isBroken(): Boolean {
        return damage >= health
    }

    fun mobBreakBlock(entity: LivingEntity?, block: Block) {
        block.world.playSound(block.location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f)
        block.world.spawnParticle(Particle.BLOCK_CRACK, block.location, 10, block.blockData)
        block.breakNaturally()
    }

    fun mobDamageBlock(entity: LivingEntity?, block: Block) {

        val location = block.location
        val world = block.world
        val damagePercentage = 1.0f * damage / health

        Bukkit.getServer().onlinePlayers.forEach { player -> player.sendBlockDamage(location, damagePercentage, blockID) }
        world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.5f, 0.5f)
        world.spawnParticle(Particle.BLOCK_CRACK, location, 5, block.blockData)
    }


    override fun toString(): String {
        return ("HealthBlock [block=" + block + ", mat=" + mat + ", loc=" + loc + ", damage="
                + damage + ", health=" + health + "]")
    }
}