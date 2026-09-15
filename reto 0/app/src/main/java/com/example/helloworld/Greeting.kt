package com.example.helloworld

data class Greeting(
    val text: String,
    val languageLabel: String,
    val backgroundColor: Int
)

object Greetings {

    val all: List<Greeting> = listOf(
        Greeting("¡Hola, Mundo!", "Español", 0xFFE53935.toInt()),
        Greeting("Hello, World!", "English", 0xFF1E88E5.toInt()),
        Greeting("Bonjour, le Monde !", "Français", 0xFF8E24AA.toInt()),
        Greeting("こんにちは、世界！", "日本語", 0xFF43A047.toInt()),
        Greeting("Hallo, Welt!", "Deutsch", 0xFFFB8C00.toInt()),
        Greeting("Olá, Mundo!", "Português", 0xFF00897B.toInt()),
        Greeting("👋 🌍 ✨", "Emoji", 0xFF3949AB.toInt())
    )

    fun next(currentIndex: Int): Pair<Greeting, Int> {
        val nextIndex = (currentIndex + 1) % all.size
        return all[nextIndex] to nextIndex
    }

    fun random(excludingIndex: Int): Pair<Greeting, Int> {
        if (all.size <= 1) return all[0] to 0
        var index: Int
        do {
            index = all.indices.random()
        } while (index == excludingIndex)
        return all[index] to index
    }
}
