package com.redfootdev.mobageddon.powers

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionEffectType

data class StrikerOptions(
    var entityType: EntityType,
    var infectionChance: Double,
    var cureItems: HashMap<Material, Double>,
    var potionEffects: HashMap<PotionEffectType, Double>
)

/**
 * Striker - Cause on hit effects to players
 * @author joshr
 */
class Striker 