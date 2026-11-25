package com.unal.reto10

import com.unal.reto10.data.BadJsonException
import com.unal.reto10.data.CollectionPointParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CollectionPointParserTest {
    private val full = """[{"programa_de_recolecci_n":"Pilas con el Ambiente","tipo_de_residuos":"Pilas",
        "departamento":"Norte de Santander","municipio":"San José de Cúcuta","codigo_dane":"54001",
        "direccion":"Av 3N # 38-42","raz_n_social":"Justo y Bueno"}]"""

    @Test fun parsesFullRow() {
        val p = CollectionPointParser.parseList(full).single()
        assertEquals("Justo y Bueno", p.businessName)
        assertEquals("Av 3N # 38-42", p.address)
        assertEquals("San José de Cúcuta", p.municipality)
        assertEquals("Pilas con el Ambiente", p.program)
        assertEquals("54001", p.daneCode)
    }

    @Test fun missingFieldsAreTolerated() {
        val p = CollectionPointParser.parseList("""[{"raz_n_social":"X"}]""").single()
        assertEquals("", p.address)
        assertNull(p.daneCode)
    }

    @Test fun rowsWithoutNameNorAddressAreSkipped() {
        assertEquals(0, CollectionPointParser.parseList("""[{"municipio":"Ocaña"}]""").size)
    }

    @Test fun emptyArray() {
        assertEquals(0, CollectionPointParser.parseList("[]").size)
    }

    @Test(expected = BadJsonException::class) fun socrataErrorObject() {
        CollectionPointParser.parseList("""{"error":true,"message":"Not found"}""")
    }

    @Test(expected = BadJsonException::class) fun garbage() {
        CollectionPointParser.parseList("<html>")
    }

    @Test fun count() {
        assertEquals(120, CollectionPointParser.parseCount("""[{"count":"120"}]"""))
        assertEquals(0, CollectionPointParser.parseCount("[]"))
    }

    @Test fun municipalities() {
        val l = CollectionPointParser.parseMunicipalities(
            """[{"municipio":"Ocaña"},{"municipio":""},{"municipio":"Abrego"}]"""
        )
        assertEquals(listOf("Ocaña", "Abrego"), l)
    }
}
