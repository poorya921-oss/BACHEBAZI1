package com.bachebazi.app.logic

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/**
 * Unbiased random selection helpers.
 *
 * The default source is java.security.SecureRandom (a strong, unpredictable generator that is
 * never seeded from time, entry order or names). Kotlin's Random.nextInt(until) uses rejection
 * sampling, so every index has exactly equal probability (no modulo bias).
 */
object Randomizer {
    private val secure: Random = SecureRandom().asKotlinRandom()

    /** Uniform index in [0, size). */
    fun pickIndex(size: Int, random: Random = secure): Int {
        require(size > 0) { "size must be positive" }
        return random.nextInt(size)
    }

    /** One uniformly chosen element, or null for an empty list. Duplicates count separately. */
    fun <T> pickOne(items: List<T>, random: Random = secure): T? =
        if (items.isEmpty()) null else items[pickIndex(items.size, random)]

    /** Fisher–Yates shuffle (returns a new list). */
    fun <T> shuffled(items: List<T>, random: Random = secure): List<T> {
        val a = items.toMutableList()
        for (i in a.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val t = a[i]; a[i] = a[j]; a[j] = t
        }
        return a
    }

    /** [count] distinct positions chosen uniformly (partial Fisher–Yates). */
    fun <T> pickDistinct(items: List<T>, count: Int, random: Random = secure): List<T> {
        val n = count.coerceIn(0, items.size)
        val a = items.toMutableList()
        for (i in 0 until n) {
            val j = i + random.nextInt(a.size - i)
            val t = a[i]; a[i] = a[j]; a[j] = t
        }
        return a.subList(0, n).toList()
    }
}
