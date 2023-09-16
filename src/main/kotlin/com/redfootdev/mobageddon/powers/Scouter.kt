package com.redfootdev.mobageddon.powers

import com.redfootdev.mobageddon.Mobageddon
import com.redfootdev.mobageddon.utils.ConfigUtil
import com.redfootdev.mobageddon.utils.RandUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import java.util.EnumMap

data class ScouterOptions(var entityType: EntityType, var hearingMultiple: Double)

/**
 * Scouter - Uses actions performed by a player to find their location
 * @author joshr
 */
class Scouter(var plugin: Mobageddon) : Listener {
    private var breakRange: Int = plugin.config.getInt("ScoutSounds.Break")
    private var placeRange: Int = plugin.config.getInt("ScoutSounds.Place")
    private var craftRange: Int = plugin.config.getInt("ScoutSounds.Craft")
    private var itemBreakRange: Int = plugin.config.getInt("ScoutSounds.ItemBreak")
    private var consumeRange: Int = plugin.config.getInt("ScoutSounds.Consume")
    private var shootRange: Int = plugin.config.getInt("ScoutSounds.Shoot")
    private var breedRange: Int = plugin.config.getInt("ScoutSounds.Breed")
    private var damageRange: Int = plugin.config.getInt("ScoutSounds.Damage")
    var options: EnumMap<EntityType, ScouterOptions> = EnumMap(EntityType::class.java)

    init {
        loadSpawningConfigurations()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun loadSpawningConfigurations() {
        var sections: HashMap<EntityType, ConfigurationSection> = ConfigUtil.getPowerSections(plugin, "Jumper")
        for (entityType in sections.keys) {
            val hearingMultiple = sections[entityType]!!.getDouble("HearingMultiple")
            options[entityType] = ScouterOptions(entityType, hearingMultiple)
        }
    }

    @EventHandler
    fun breakSound(event: BlockBreakEvent) {
        makeSound(event.player, breakRange)
    }

    @EventHandler
    fun placeSound(event: BlockPlaceEvent) {
        makeSound(event.player, placeRange)
    }

    @EventHandler
    fun itemBreakSound(event: PlayerItemBreakEvent) {
        makeSound(event.player, itemBreakRange)
    }

    @EventHandler
    fun consumeSound(event: PlayerItemConsumeEvent) {
        makeSound(event.player, consumeRange)
    }

    @EventHandler
    fun consumeSound(event: EntityShootBowEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        makeSound(player, shootRange)
    }

    @EventHandler
    fun consumeSound(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        makeSound(player, damageRange)
    }

    @EventHandler
    fun consumeSound(event: EntityBreedEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        makeSound(player, breedRange)
    }

    @EventHandler
    fun craftSound(event: CraftItemEvent) {
        if (event.whoClicked !is Player) return
        val player = event.whoClicked as Player
        makeSound(player, breakRange)
    }

    private fun makeSound(soundMaker: Player, soundRange: Int) {
        // If not an enabled world, return
        var actualSoundRange = soundRange
        if (!plugin.enabledWorlds.contains(soundMaker.world)) return
        if (RandUtil.chance(1, 100)) {
            actualSoundRange *= 4
        } else if (RandUtil.chance(1, 20)) {
            actualSoundRange *= 2
        }
        if (soundMaker.isSneaking) {
            actualSoundRange /= 2
        }
        for (entity in soundMaker.getNearbyEntities(
            actualSoundRange.toDouble(),
            actualSoundRange.toDouble(),
            actualSoundRange.toDouble()
        )) {
            if (entity is Monster) {
                if (options.containsKey(entity.getType())) {
                    entity.target = soundMaker
                }
            }
        }
    }
}