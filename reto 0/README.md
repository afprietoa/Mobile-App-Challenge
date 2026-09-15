# Hello World Multi-idioma Interactivo (Kotlin)

Reinterpretación creativa del clásico "Hello World" de Android, escrita 100% en Kotlin sobre el sistema de Vistas (sin Jetpack Compose).

## Qué hace

- Al abrir la app, el saludo aparece con un efecto **máquina de escribir** y una animación de entrada (fade + scale con rebote).
- **Toca la pantalla** para ciclar al siguiente saludo: cambia el texto, el idioma y el color de fondo con una transición animada.
- **Agita el dispositivo** para recibir un saludo **aleatorio**, acompañado de una vibración corta.
- El color del texto se calcula dinámicamente según la luminosidad del fondo, para mantener siempre buen contraste.
- Soporta modo claro/oscuro y pantalla edge-to-edge.

Idiomas incluidos: Español, English, Français, 日本語, Deutsch, Português y una variante en emoji.

## Estructura del código

- `MainActivity.kt` — ciclo de vida, animaciones, listeners de tap y sensor, vibración.
- `Greeting.kt` — modelo de datos (`Greeting`) y la fuente de saludos (`Greetings`), con la lógica de ciclo y selección aleatoria.
- `activity_main.xml` — layout con el saludo, la etiqueta de idioma y el texto de instrucciones.

## Cómo probarla

1. Ejecutar la app en un emulador o dispositivo físico.
2. Tocar la pantalla varias veces para recorrer los saludos.
3. Agitar el dispositivo (o usar los controles de sensor del emulador) para un saludo aleatorio.

## Tests

- `app/src/test/.../GreetingsTest.kt`: pruebas unitarias de la lógica de ciclo y selección aleatoria (`./gradlew testDebugUnitTest`).
- `app/src/androidTest/.../MainActivityTest.kt`: prueba instrumentada con Espresso que verifica que un tap cambia el saludo mostrado (`./gradlew connectedAndroidTest`, requiere emulador o dispositivo conectado).
