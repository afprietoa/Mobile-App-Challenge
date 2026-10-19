package com.unal.reto8.data

import android.content.ContentValues
import android.database.Cursor
import com.unal.reto8.data.CompanyDbHelper.Companion.CLASSIFICATION
import com.unal.reto8.data.CompanyDbHelper.Companion.EMAIL
import com.unal.reto8.data.CompanyDbHelper.Companion.ID
import com.unal.reto8.data.CompanyDbHelper.Companion.NAME
import com.unal.reto8.data.CompanyDbHelper.Companion.PHONE
import com.unal.reto8.data.CompanyDbHelper.Companion.PRODUCTS
import com.unal.reto8.data.CompanyDbHelper.Companion.TABLE
import com.unal.reto8.data.CompanyDbHelper.Companion.WEBSITE
import com.unal.reto8.model.Company

class SqliteCompanyRepository(private val helper: CompanyDbHelper) : CompanyRepository {

    override fun insert(company: Company): Long =
        helper.writableDatabase.insert(TABLE, null, company.toValues())

    override fun getById(id: Long): Company? =
        helper.readableDatabase.query(TABLE, null, "$ID = ?", arrayOf(id.toString()), null, null, null)
            .use { if (it.moveToFirst()) it.toCompany() else null }

    override fun update(company: Company): Boolean =
        helper.writableDatabase.update(TABLE, company.toValues(), "$ID = ?", arrayOf(company.id.toString())) > 0

    override fun delete(id: Long): Boolean =
        helper.writableDatabase.delete(TABLE, "$ID = ?", arrayOf(id.toString())) > 0

    override fun search(filter: CompanyFilter): List<Company> {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (filter.nameQuery.isNotBlank()) {
            where += "$NAME LIKE ? ESCAPE '\\'"
            args += LikeEscaper.contains(filter.nameQuery)
        }
        if (filter.classificationMask != 0) {
            where += "($CLASSIFICATION & ?) != 0"
            args += filter.classificationMask.toString()
        }
        return helper.readableDatabase.query(
            TABLE, null,
            where.joinToString(" AND ").ifEmpty { null },
            args.toTypedArray().ifEmpty { null },
            null, null, "$NAME COLLATE NOCASE ASC"
        ).use { c -> buildList { while (c.moveToNext()) add(c.toCompany()) } }
    }

    private fun Company.toValues() = ContentValues().apply {
        put(NAME, name.trim())
        put(WEBSITE, website.trim())
        put(PHONE, phone.trim())
        put(EMAIL, email.trim())
        put(PRODUCTS, products.trim())
        put(CLASSIFICATION, classification)
    }

    private fun Cursor.toCompany() = Company(
        id = getLong(getColumnIndexOrThrow(ID)),
        name = getString(getColumnIndexOrThrow(NAME)),
        website = getString(getColumnIndexOrThrow(WEBSITE)),
        phone = getString(getColumnIndexOrThrow(PHONE)),
        email = getString(getColumnIndexOrThrow(EMAIL)),
        products = getString(getColumnIndexOrThrow(PRODUCTS)),
        classification = getInt(getColumnIndexOrThrow(CLASSIFICATION))
    )
}
