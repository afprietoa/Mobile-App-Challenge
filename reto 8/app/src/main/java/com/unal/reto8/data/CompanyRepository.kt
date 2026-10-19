package com.unal.reto8.data

import com.unal.reto8.model.Company

/** [nameQuery] vacío = sin filtro de nombre; [classificationMask] 0 = sin filtro de clasificación. */
data class CompanyFilter(val nameQuery: String = "", val classificationMask: Int = 0)

interface CompanyRepository {
    fun insert(company: Company): Long                 // id nuevo, o -1 si falló
    fun getById(id: Long): Company?
    fun update(company: Company): Boolean              // true si se modificó una fila
    fun delete(id: Long): Boolean                      // true si se eliminó una fila
    fun search(filter: CompanyFilter = CompanyFilter()): List<Company>
}
