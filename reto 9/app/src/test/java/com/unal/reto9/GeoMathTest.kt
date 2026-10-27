package com.unal.reto9

import com.unal.reto9.model.GeoMath
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoMathTest {
    @Test
    fun distanceToSelfIsZero() {
        assertEquals(0.0, GeoMath.distanceMeters(4.6, -74.0, 4.6, -74.0), 0.001)
    }

    @Test
    fun bogotaToMedellinIsAbout240Km() {
        val d = GeoMath.distanceMeters(4.7110, -74.0721, 6.2442, -75.5812)
        assertEquals(240_000.0, d, 2_000.0)
    }

    @Test
    fun distanceIsSymmetric() {
        val ab = GeoMath.distanceMeters(4.7, -74.0, 6.2, -75.5)
        val ba = GeoMath.distanceMeters(6.2, -75.5, 4.7, -74.0)
        assertEquals(ab, ba, 0.001)
    }

    @Test
    fun oneDegreeOfLatitudeIsAbout111Km() {
        assertEquals(111_200.0, GeoMath.distanceMeters(0.0, 0.0, 1.0, 0.0), 300.0)
    }
}
