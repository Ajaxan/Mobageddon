package com.redfootdev.mobageddon.utils

import java.util.*

object RandUtil {
    fun chance(chance: Double, `in`: Double): Boolean {
        return Math.random() * `in` <= chance - 1
    }

    fun chance(chance: Int, `in`: Int): Boolean {
        return (Math.random() * `in`).toInt() <= chance - 1
    }

    // Includes Max!
    fun range(min: Int, max: Int): Int {
        return Random().nextInt(max - min + 1) + min
    }

    fun fromlist(nums: IntArray): Int {
        return nums[range(0, nums.size - 1)]
    }

    fun <E> fromcollection(e: Collection<E>): Optional<E> {
        return e.stream()
            .skip((e.size * Math.random()).toInt().toLong())
            .findFirst()
    }

    @JvmStatic
	fun randomInt(): Int {
        return Random().nextInt()
    }
}