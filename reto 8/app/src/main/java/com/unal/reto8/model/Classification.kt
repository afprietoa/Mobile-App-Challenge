package com.unal.reto8.model

import androidx.annotation.StringRes
import com.unal.reto8.R

/** Tipos de empresa. [bit] es la posición en la máscara guardada en SQLite. */
enum class Classification(val bit: Int, @StringRes val label: Int) {
    CONSULTING(1, R.string.class_consulting),
    CUSTOM_DEVELOPMENT(2, R.string.class_custom),
    SOFTWARE_FACTORY(4, R.string.class_factory);

    companion object {
        fun toMask(items: Collection<Classification>): Int = items.fold(0) { acc, c -> acc or c.bit }
        fun fromMask(mask: Int): Set<Classification> = entries.filter { mask and it.bit != 0 }.toSet()
    }
}
