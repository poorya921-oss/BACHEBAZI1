package com.bachebazi.app

import com.bachebazi.app.logic.Randomizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlin.random.Random
import org.junit.Test

class RandomizerTest {
    @Test fun emptyListReturnsNull() = assertNull(Randomizer.pickOne(emptyList<String>()))

    @Test fun singleEntryAlwaysSelected() {
        repeat(50) { assertEquals("a", Randomizer.pickOne(listOf("a"))) }
    }

    @Test fun resultAlwaysBelongsToList() {
        val items = listOf("x", "y", "z", "y")
        repeat(1000) { assertTrue(Randomizer.pickOne(items) in items) }
    }

    @Test fun distributionIsRoughlyUniformAndDuplicatesCountSeparately() {
        val items = listOf("a", "b", "c", "c") // "c" should win ~50%
        val counts = HashMap<String, Int>()
        val rnd = Random(12345) // seeded only to make the test deterministic
        val n = 40_000
        repeat(n) { counts.merge(Randomizer.pickOne(items, rnd)!!, 1, Int::plus) }
        assertEquals(0.25, counts["a"]!!.toDouble() / n, 0.02)
        assertEquals(0.25, counts["b"]!!.toDouble() / n, 0.02)
        assertEquals(0.50, counts["c"]!!.toDouble() / n, 0.02)
    }

    @Test fun shuffleIsAPermutation() {
        val items = (1..50).toList()
        assertEquals(items, Randomizer.shuffled(items).sorted())
    }

    @Test fun pickDistinctReturnsDistinctPositions() {
        val items = (1..20).toList()
        val r = Randomizer.pickDistinct(items, 7)
        assertEquals(7, r.size)
        assertEquals(7, r.toSet().size)
        assertEquals(20, Randomizer.pickDistinct(items, 99).size)
    }
}
