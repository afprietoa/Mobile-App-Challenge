package com.unal.reto8

import com.unal.reto8.model.Company
import com.unal.reto8.model.CompanyValidator
import com.unal.reto8.model.Field
import com.unal.reto8.model.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanyValidatorTest {

    private fun valid() = Company(name = "Acme", classification = 1)
    private fun errors(c: Company) = CompanyValidator.validate(c)

    @Test fun blankName_isRequired() =
        assertEquals(FieldError.REQUIRED, errors(valid().copy(name = "  "))[Field.NAME])

    @Test fun noClassification_isRequired() =
        assertEquals(FieldError.REQUIRED, errors(valid().copy(classification = 0))[Field.CLASSIFICATION])

    @Test fun email_validation() {
        assertEquals(FieldError.INVALID, errors(valid().copy(email = "a@b"))[Field.EMAIL])
        assertNull(errors(valid().copy(email = "a@b.co"))[Field.EMAIL])
        assertNull(errors(valid().copy(email = ""))[Field.EMAIL])
    }

    @Test fun phone_validation() {
        assertEquals(FieldError.INVALID, errors(valid().copy(phone = "123"))[Field.PHONE])
        assertNull(errors(valid().copy(phone = "+57 601 555 1234"))[Field.PHONE])
        assertNull(errors(valid().copy(phone = ""))[Field.PHONE])
    }

    @Test fun website_validation() {
        assertEquals(FieldError.INVALID, errors(valid().copy(website = "localhost"))[Field.WEBSITE])
        assertNull(errors(valid().copy(website = "unal.edu.co"))[Field.WEBSITE])
    }

    @Test fun completeCompany_hasNoErrors() {
        val c = Company(
            name = "Acme", website = "https://acme.co", phone = "3001234567",
            email = "info@acme.co", products = "ERP", classification = 7
        )
        assertTrue(errors(c).isEmpty())
    }

    @Test fun normalizeUrl() {
        assertEquals("https://unal.edu.co", CompanyValidator.normalizeUrl("unal.edu.co"))
        assertEquals("http://x.co", CompanyValidator.normalizeUrl("http://x.co"))
        assertEquals("", CompanyValidator.normalizeUrl("  "))
    }
}
