# Reto 11 — Tutor IA (chatbot con Gemini)

App Android en Kotlin que integra un modelo de IA generativa (Google **Gemini**, API REST) como chatbot "Tutor de Desarrollo Móvil", con plantillas de prompt y ajustes del *system prompt* y la temperatura.

## Configuración

1. Crea una API key en <https://aistudio.google.com/apikey>.
2. En `local.properties` (no se versiona) agrega: `GEMINI_API_KEY=tu_clave`.
3. Compila y ejecuta: `./gradlew :app:assembleDebug` (o desde Android Studio).

Sin clave, la app muestra un aviso y no hace llamadas. El modelo se fija en `GeminiConfig.MODEL` (`gemini-3.5-flash`); si ya no existe, listar los vigentes con `GET https://generativelanguage.googleapis.com/v1beta/models` (cabecera `x-goog-api-key`).

## Qué hace

- Chat con memoria de contexto (se envían los últimos 20 mensajes), burbujas usuario/modelo, indicador "Escribiendo…", botón **Detener** y **Reintentar** ante errores.
- Menú: **Nueva conversación**, **Plantillas de prompt** (Explicar, Resumir, Quiz, Corregir código) y **Ajustes** (system prompt, temperatura).
- **Imágenes:** pide "Genera una imagen de …" (o dibuja / crea un logo de …) y la respuesta muestra la imagen en la burbuja; se guarda en el almacenamiento interno y persiste con el historial.
- Mantener pulsada una respuesta: copiar o compartir. Markdown básico (negrita, código en línea y en bloque, viñetas).
- El historial y los ajustes persisten en `SharedPreferences`; rotar la pantalla no reenvía la petición.

## Arquitectura

```
model/   Message, ChatReply, ChatError, ChatState
data/    GeminiRequestBuilder, GeminiResponseParser, GeminiChatRepository (HttpURLConnection + org.json),
         ChatRepository (interfaz), ChatHistoryTrimmer, PromptTemplate, ChatStore, GeminiConfig
ui/      MainActivity, ChatViewModel, MessageAdapter, MarkdownFormatter
```

Sin SDKs ni librerías de red. Permisos: `INTERNET`, `ACCESS_NETWORK_STATE`. Solo HTTPS.

## Pruebas

`./gradlew :app:testDebugUnitTest` — request builder, parser, trimmer, plantillas y `ChatViewModel` (con repositorio *fake*). Ver `TESTING.md` para la checklist manual.

## Limitaciones y uso responsable

- La API key queda embebida en el APK (extraíble): aceptable para un reto académico; en producción se usaría un backend.
- La capa gratuita tiene cuota (error 429 → "intenta más tarde").
- Sin streaming. Las imágenes se generan con Pollinations (servicio gratuito de terceros, sin clave): el prompt de imagen se envía a ese proveedor y su calidad y disponibilidad varían. El modelo puede equivocarse.
- Lo que escribes se envía a Google: no incluyas datos personales.
