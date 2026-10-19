package com.unal.reto8

import com.unal.reto8.model.Classification
import com.unal.reto8.model.Classification.CONSULTING
import com.unal.reto8.model.Classification.CUSTOM_DEVELOPMENT
import com.unal.reto8.model.Classification.SOFTWARE_FACTORY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassificationTest {

    @Test fun toMask_combinesBits() =
        assertEquals(5, Classification.toMask(setOf(CONSULTING, SOFTWARE_FACTORY)))

    @Test fun fromMask_extractsSet() =
        assertEquals(setOf(CUSTOM_DEVELOPMENT, SOFTWARE_FACTORY), Classification.fromMask(6))

    @Test fun fromMask_zeroIsEmpty() = assertTrue(Classification.fromMask(0).isEmpty())

    @Test fun roundTrip_allCombinations() {
        for (mask in 0..7) {
            assertEquals(mask, Classification.toMask(Classification.fromMask(mask)))
        }
    }
}
