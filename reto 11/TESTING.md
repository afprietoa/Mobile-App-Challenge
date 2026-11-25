# Verificación — Reto 11

## Automática

`./gradlew :app:testDebugUnitTest` (JDK completo, con `jlink`).

## Checklist manual (requiere dispositivo/emulador y API key)

- [ ] Conversación de 5+ turnos: el modelo recuerda el contexto.
- [ ] Sin API key → aviso en pantalla y error "No hay API key", sin caídas.
- [ ] API key inválida → "La API key es inválida o no tiene permisos".
- [ ] Texto con tildes, `ñ`, emojis, comillas y saltos de línea.
- [ ] Modo avión → error con **Reintentar**; al volver la red, reintentar funciona.
- [ ] Pregunta que active filtros de seguridad → "Contenido bloqueado…".
- [ ] Rotar con una respuesta pendiente: no se duplica la petición ni se pierden mensajes.
- [ ] Matar la app y reabrir: el historial se restaura; **Nueva conversación** lo borra.
- [ ] Plantilla "Quiz" con tema "Intents" → prompt estructurado y respuesta tipo quiz.
- [ ] Cambiar el system prompt en Ajustes cambia el comportamiento.
- [ ] Mantener pulsada una respuesta: copiar y compartir funcionan.
- [ ] Cuota excedida (429) → "Intenta más tarde".
- [ ] Tema oscuro legible.
