package com.unal.reto8.model

enum class Field { NAME, WEBSITE, PHONE, EMAIL, CLASSIFICATION }
enum class FieldError { REQUIRED, INVALID }

object CompanyValidator {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE = Regex("^[+]?[0-9 ()-]{7,20}$")

    /** Devuelve los errores por campo; mapa vacío = válida. Los opcionales solo se validan si se llenan. */
    fun validate(company: Company): Map<Field, FieldError> {
        val errors = mutableMapOf<Field, FieldError>()
        if (company.name.isBlank()) errors[Field.NAME] = FieldError.REQUIRED
        if (company.classification == 0) errors[Field.CLASSIFICATION] = FieldError.REQUIRED
        if (company.email.isNotBlank() && !EMAIL.matches(company.email.trim())) errors[Field.EMAIL] = FieldError.INVALID
        if (company.phone.isNotBlank() && !PHONE.matches(company.phone.trim())) errors[Field.PHONE] = FieldError.INVALID
        if (company.website.isNotBlank() && !company.website.trim().contains('.')) errors[Field.WEBSITE] = FieldError.INVALID
        return errors
    }

    /** "unal.edu.co" -> "https://unal.edu.co"; vacío se queda vacío. */
    fun normalizeUrl(raw: String): String {
        val t = raw.trim()
        return when {
            t.isEmpty() -> ""
            t.startsWith("http://", true) || t.startsWith("https://", true) -> t
            else -> "https://$t"
        }
    }
}
