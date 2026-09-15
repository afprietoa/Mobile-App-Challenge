package com.example.helloworld

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GreetingsTest {

    @Test
    fun all_isNotEmpty() {
        assertTrue(Greetings.all.isNotEmpty())
    }

    @Test
    fun next_cyclesForwardThroughList() {
        val (greeting, index) = Greetings.next(0)
        assertEquals(1, index)
        assertEquals(Greetings.all[1], greeting)
    }

    @Test
    fun next_wrapsAroundToTheStart() {
        val lastIndex = Greetings.all.lastIndex
        val (greeting, index) = Greetings.next(lastIndex)
        assertEquals(0, index)
        assertEquals(Greetings.all[0], greeting)
    }

    @Test
    fun random_neverReturnsTheExcludedIndex() {
        val excluded = 2
        repeat(50) {
            val (_, index) = Greetings.random(excluded)
            assertNotEquals(excluded, index)
        }
    }
}
