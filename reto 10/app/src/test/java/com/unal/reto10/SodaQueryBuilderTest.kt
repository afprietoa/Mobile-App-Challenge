package com.unal.reto10

import com.unal.reto10.data.SodaQueryBuilder
import com.unal.reto10.model.SodaFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SodaQueryBuilderTest {
    @Test fun emptyFilterHasNoWhereNorQ() {
        val url = SodaQueryBuilder.list(SodaFilter())
        assertFalse(url.contains("%24where"))
        assertFalse(url.contains("%24q="))
        assertTrue(url.contains("%24limit=20"))
        assertTrue(url.contains("%24offset=0"))
        assertTrue(url.contains("%24order=raz_n_social"))
    }

    @Test fun municipalityWithAccentIsEncoded() {
        val url = SodaQueryBuilder.list(SodaFilter(municipality = "Ocaña"))
        assertTrue(url, url.contains("%24where=municipio%3D%27Oca%C3%B1a%27"))
    }

    @Test fun apostropheIsDoubled() {
        assertEquals("O''Brien", SodaQueryBuilder.escape("O'Brien"))
        val url = SodaQueryBuilder.list(SodaFilter(municipality = "O'B"))
        assertTrue(url.contains("%27O%27%27B%27"))
    }

    @Test fun textAndPaging() {
        val url = SodaQueryBuilder.list(SodaFilter(text = " pilas & más ", pageSize = 20, offset = 40))
        assertTrue(url, url.contains("%24q=pilas%20%26%20m%C3%A1s"))
        assertTrue(url.contains("%24offset=40"))
    }

    @Test fun blankMunicipalityMeansAll() {
        assertFalse(SodaQueryBuilder.list(SodaFilter(municipality = "  ")).contains("where"))
    }

    @Test fun pageSizeAndOffsetAreClamped() {
        val url = SodaQueryBuilder.list(SodaFilter(pageSize = 5000, offset = -3))
        assertTrue(url.contains("%24limit=100"))
        assertTrue(url.contains("%24offset=0"))
    }

    @Test fun countUsesSameFilters() {
        val url = SodaQueryBuilder.count(SodaFilter(municipality = "Ocaña", text = "pilas"))
        assertTrue(url.contains("%24select=count%28*%29"))
        assertTrue(url.contains("%24where="))
        assertTrue(url.contains("%24q=pilas"))
        assertFalse(url.contains("limit"))
    }

    @Test fun municipalitiesQuery() {
        val url = SodaQueryBuilder.municipalities()
        assertTrue(url.contains("distinct%20municipio"))
        assertTrue(url.startsWith("https://"))
    }
}
