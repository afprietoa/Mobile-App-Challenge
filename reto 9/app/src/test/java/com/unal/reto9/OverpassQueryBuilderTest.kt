package com.unal.reto9

import com.unal.reto9.data.OverpassQueryBuilder
import com.unal.reto9.model.PoiCategory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class OverpassQueryBuilderTest {
    private val originalLocale = Locale.getDefault()

    @After
    fun restoreLocale() = Locale.setDefault(originalLocale)

    @Test
    fun usesDotDecimalsEvenWithSpanishLocale() {
        Locale.setDefault(Locale("es", "CO"))
        val q = OverpassQueryBuilder.build(4.6097, -74.0817, 3000, setOf(PoiCategory.HOSPITAL))
        assertTrue(q, q.contains("around:3000,4.609700,-74.081700"))
    }

    @Test
    fun oneClausePerCategory() {
        val q = OverpassQueryBuilder.build(4.0, -74.0, 1000, setOf(PoiCategory.HOSPITAL, PoiCategory.ATTRACTION))
        assertTrue(q.contains("\"amenity\"=\"hospital\""))
        assertTrue(q.contains("\"tourism\"=\"attraction\""))
        assertEquals(2, q.lines().count { it.contains("around:") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyCategoriesAreRejected() {
        OverpassQueryBuilder.build(4.0, -74.0, 1000, emptySet())
    }

    @Test
    fun endsWithOutClause() {
        val q = OverpassQueryBuilder.build(4.0, -74.0, 1000, setOf(PoiCategory.PARK))
        assertTrue(q.endsWith("out center tags 100;"))
    }
}
