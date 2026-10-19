package com.unal.reto8

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.reto8.data.CompanyDbHelper
import com.unal.reto8.data.CompanyFilter
import com.unal.reto8.data.SqliteCompanyRepository
import com.unal.reto8.model.Classification
import com.unal.reto8.model.Company
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteCompanyRepositoryTest {

    private lateinit var helper: CompanyDbHelper
    private lateinit var repo: SqliteCompanyRepository

    private val consulting = Classification.CONSULTING.bit
    private val custom = Classification.CUSTOM_DEVELOPMENT.bit
    private val factory = Classification.SOFTWARE_FACTORY.bit

    @Before fun setUp() {
        // name = null -> BD en memoria, aislada en cada test
        helper = CompanyDbHelper(ApplicationProvider.getApplicationContext(), null)
        repo = SqliteCompanyRepository(helper)
    }

    @After fun tearDown() = helper.close()

    private fun add(name: String, mask: Int = consulting) = repo.insert(Company(name = name, classification = mask))

    private fun names(filter: CompanyFilter = CompanyFilter()) = repo.search(filter).map { it.name }

    @Test fun insert_thenGetById_returnsSameData() {
        val company = Company(
            name = "Acme", website = "https://acme.co", phone = "3001234567",
            email = "info@acme.co", products = "ERP", classification = consulting or factory
        )
        val id = repo.insert(company)
        assertTrue(id > 0)
        assertEquals(company.copy(id = id), repo.getById(id))
    }

    @Test fun update_changesFields() {
        val id = add("Acme")
        assertTrue(repo.update(Company(id = id, name = "Acme 2", classification = custom)))
        val saved = repo.getById(id)!!
        assertEquals("Acme 2", saved.name)
        assertEquals(custom, saved.classification)
    }

    @Test fun update_unknownId_returnsFalse() =
        assertFalse(repo.update(Company(id = 999, name = "Nadie", classification = consulting)))

    @Test fun delete_removesRow_andSecondDeleteReturnsFalse() {
        val id = add("Acme")
        assertTrue(repo.delete(id))
        assertNull(repo.getById(id))
        assertFalse(repo.delete(id))
    }

    @Test fun search_withoutFilter_isOrderedIgnoringCase() {
        add("beta"); add("Zeta"); add("Alfa")
        assertEquals(listOf("Alfa", "beta", "Zeta"), names())
    }

    @Test fun search_byName_isContainsAndCaseInsensitive() {
        add("Alfa Soft"); add("Beta"); add("Malfa")
        assertEquals(listOf("Alfa Soft", "Malfa"), names(CompanyFilter(nameQuery = "ALF")))
    }

    @Test fun search_percentIsNotWildcard() {
        add("Acme"); add("50% Soft")
        assertEquals(listOf("50% Soft"), names(CompanyFilter(nameQuery = "50%")))
        assertEquals(listOf("50% Soft"), names(CompanyFilter(nameQuery = "%")))
    }

    @Test fun search_byClassification_includesCombinedOnes() {
        add("A", consulting); add("B", custom); add("C", consulting or custom)
        assertEquals(listOf("A", "C"), names(CompanyFilter(classificationMask = consulting)))
    }

    @Test fun search_byTwoClassifications_isOr() {
        add("A", consulting); add("B", custom); add("C", factory)
        assertEquals(listOf("A", "B"), names(CompanyFilter(classificationMask = consulting or custom)))
    }

    @Test fun search_nameAndClassification_isAnd() {
        add("Alfa", consulting); add("Alfa Dos", custom); add("Beta", consulting)
        assertEquals(listOf("Alfa"), names(CompanyFilter("alfa", consulting)))
    }

    @Test fun specialCharacters_travelIntact() {
        val id = repo.insert(Company(name = "O'Reilly; DROP TABLE companies;--", products = "Ágil ñandú 😀", classification = consulting))
        val saved = repo.getById(id)
        assertNotNull(saved)
        assertEquals("O'Reilly; DROP TABLE companies;--", saved!!.name)
        assertEquals("Ágil ñandú 😀", saved.products)
        assertEquals(1, repo.search().size) // la tabla sigue existiendo
    }
}
