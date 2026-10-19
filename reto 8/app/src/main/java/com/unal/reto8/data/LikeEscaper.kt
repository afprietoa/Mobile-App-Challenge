package com.unal.reto8.data

/** Escapa % y _ para que el usuario busque literalmente (se usa con `ESCAPE '\'`). */
object LikeEscaper {
    fun contains(text: String): String =
        "%" + text.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
}
