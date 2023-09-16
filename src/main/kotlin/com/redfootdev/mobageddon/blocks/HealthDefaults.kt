package com.redfootdev.mobageddon.blocks

import com.redfootdev.mobageddon.Mobageddon
import org.bukkit.Material

class HealthDefaults(var plugin: Mobageddon) {

    companion object {
        var materialHealth = HashMap<Material, Int>()
        fun getMatHealth(mat: Material): Int {
            return if (materialHealth.containsKey(mat)) {
                materialHealth[mat]!!
            } else 10
        }
    }

    fun loadDefaults() {
        val config = plugin.config
        val confsec = config.getConfigurationSection("BlockHealth")!!
        for (matName in confsec.getKeys(false)) {
            val mat = Material.getMaterial(matName!!)
            if (mat != null) {
                val health = confsec.getInt(matName)
                materialHealth[mat] = health
            }
        }
    }
}