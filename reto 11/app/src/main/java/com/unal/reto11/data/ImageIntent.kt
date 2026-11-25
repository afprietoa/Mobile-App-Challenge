package com.unal.reto11.data

/** Detecta si el usuario pidió una imagen y extrae la descripción para el generador. */
object ImageIntent {
    private val verb = Regex(
        """\b(genera|generar|generame|génerame|crea|crear|créame|dibuja|dibujar|dibújame|haz|hazme|dame|""" +
            """muestra|muéstrame|quiero|necesito|generate|create|draw|make)\b""",
        RegexOption.IGNORE_CASE
    )
    private val nounPattern =
        """\b(imagen|imagenes|imágenes|foto|fotos|ilustración|ilustracion|dibujo|logo|logotipo|picture|image)\b"""
    private val noun = Regex(nounPattern, RegexOption.IGNORE_CASE)
    private val lead = Regex("""^.*?""" + nounPattern + """\s*(de|del|sobre|of|about|:)?\s*""", RegexOption.IGNORE_CASE)

    /** Devuelve la descripción de la imagen, o null si el texto no pide una imagen. */
    fun promptFor(text: String): String? {
        if (!verb.containsMatchIn(text) || !noun.containsMatchIn(text)) return null
        val stripped = lead.replace(text.trim(), "").trim()
        return stripped.ifBlank { text.trim() }
    }
}
