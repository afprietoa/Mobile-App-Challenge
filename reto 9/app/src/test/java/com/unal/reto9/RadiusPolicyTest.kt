package com.unal.reto9

import com.unal.reto9.model.RadiusPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class RadiusPolicyTest {
    @Test
    fun clampsBelowMinimum() {
        assertEquals(1, RadiusPolicy.clampKm(0))
        assertEquals(1, RadiusPolicy.clampKm(-5))
    }

    @Test
    fun clampsAboveMaximum() = assertEquals(20, RadiusPolicy.clampKm(99))

    @Test
    fun keepsValidValues() = assertEquals(5, RadiusPolicy.clampKm(5))

    @Test
    fun convertsToMeters() {
        assertEquals(3000, RadiusPolicy.toMeters(3))
        assertEquals(1000, RadiusPolicy.toMeters(0))
    }
}
