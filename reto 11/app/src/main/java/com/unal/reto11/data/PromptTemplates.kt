package com.unal.reto11.data

enum class PromptTemplate(val label: String, private val pattern: String, val inputHint: String) {
    EXPLAIN("Explicar", "Explícame en términos sencillos y con un ejemplo en Kotlin: %s", "Tema"),
    SUMMARIZE("Resumir", "Resume en máximo 5 viñetas el siguiente texto:\n%s", "Texto a resumir"),
    QUIZ(
        "Quiz",
        "Genera un quiz de 5 preguntas de opción múltiple sobre \"%s\", con la respuesta correcta al final.",
        "Tema del quiz"
    ),
    FIX_CODE(
        "Corregir código",
        "Revisa este código Kotlin, encuentra errores y propón una versión corregida:\n%s",
        "Código"
    );

    /** Devuelve el prompt final, o null si la entrada está vacía. */
    fun apply(input: String): String? {
        val clean = input.trim()
        return if (clean.isEmpty()) null else pattern.replace("%s", clean)
    }
}
