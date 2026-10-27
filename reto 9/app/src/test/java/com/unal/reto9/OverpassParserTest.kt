package com.unal.reto9

import com.unal.reto9.data.OverpassParser
import com.unal.reto9.model.PoiCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverpassParserTest {
    private val originLat = 4.6000
    private val originLon = -74.0800

    private fun json(vararg elements: String) = """{"elements":[${elements.joinToString(",")}]}"""

    private val farNode = """{"type":"node","id":1,"lat":4.6500,"lon":-74.0800,
        "tags":{"amenity":"hospital","name":"Hospital Lejano"}}"""
    private val nearWay = """{"type":"way","id":2,"center":{"lat":4.6010,"lon":-74.0800},
        "tags":{"tourism":"attraction","name":"Plaza \"Cercana\" Ñandú"}}"""
    private val unnamed = """{"type":"node","id":3,"lat":4.6,"lon":-74.08,"tags":{"amenity":"hospital"}}"""

    @Test
    fun parsesNodesAndWayCenters() {
        val pois = OverpassParser.parse(json(farNode, nearWay), originLat, originLon)
        assertEquals(2, pois.size)
        assertEquals(PoiCategory.ATTRACTION, pois[0].category)
        assertEquals(4.6010, pois[0].lat, 1e-9)
    }

    @Test
    fun discardsElementsWithoutName() {
        assertTrue(OverpassParser.parse(json(unnamed), originLat, originLon).isEmpty())
    }

    @Test
    fun sortedByDistanceAscending() {
        val pois = OverpassParser.parse(json(farNode, nearWay), originLat, originLon)
        assertEquals(listOf(2L, 1L), pois.map { it.id })
        assertTrue(pois[0].distanceMeters < pois[1].distanceMeters)
    }

    @Test
    fun emptyElementsGiveEmptyList() {
        assertTrue(OverpassParser.parse(json(), originLat, originLon).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun truncatedJsonThrows() {
        OverpassParser.parse("""{"elements":[{"id":1""", originLat, originLon)
    }

    @Test
    fun keepsAccentsAndEscapedQuotes() {
        val pois = OverpassParser.parse(json(nearWay), originLat, originLon)
        assertEquals("Plaza \"Cercana\" Ñandú", pois[0].name)
    }
}
