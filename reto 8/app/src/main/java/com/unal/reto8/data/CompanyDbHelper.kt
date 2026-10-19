package com.unal.reto8.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** [name] = null crea una BD en memoria (se usa en los tests instrumentados). */
class CompanyDbHelper(context: Context, name: String? = DB_NAME) :
    SQLiteOpenHelper(context, name, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $ID             INTEGER PRIMARY KEY AUTOINCREMENT,
                $NAME           TEXT    NOT NULL,
                $WEBSITE        TEXT    NOT NULL DEFAULT '',
                $PHONE          TEXT    NOT NULL DEFAULT '',
                $EMAIL          TEXT    NOT NULL DEFAULT '',
                $PRODUCTS       TEXT    NOT NULL DEFAULT '',
                $CLASSIFICATION INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    // v1: no hay migraciones todavía. Si el esquema cambia: subir DB_VERSION y hacer ALTER TABLE aquí
    // (evitar DROP TABLE: borraría los datos del usuario).
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    companion object {
        const val DB_NAME = "companies.db"
        const val DB_VERSION = 1
        const val TABLE = "companies"
        const val ID = "_id"
        const val NAME = "name"
        const val WEBSITE = "website"
        const val PHONE = "phone"
        const val EMAIL = "email"
        const val PRODUCTS = "products"
        const val CLASSIFICATION = "classification"
    }
}
