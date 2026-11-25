# Pruebas — Reto 10

## Automáticas

- Unitarias (JVM, sin emulador): `./gradlew :app:testDebugUnitTest`
  - `SodaQueryBuilderTest`, `CollectionPointParserTest`, `MainViewModelTest`.
- Instrumentadas (emulador o dispositivo): `./gradlew :app:connectedDebugAndroidTest`
  - `MainActivityTest` usa `FakeCollectionPointRepository` (sin internet): resultados + contador, estado vacío, error + reintento, abrir detalle, rotación sin nueva petición.
- Si Gradle falla con `jlink executable ... does not exist`, usa un JDK completo (`JAVA_HOME`).

## Lista de verificación manual

1. Búsqueda sin filtros: primera página de 20 y "cargar más" (scroll) hasta agotar los 120 registros.
2. Cada municipio de la lista; municipios con tilde y espacios (Ocaña, San José de Cúcuta).
3. Texto con caracteres especiales (`'`, `%`, `&`, emoji): no rompe la petición.
4. Modo avión → mensaje "Sin conexión" + Reintentar; red lenta/timeout; reintentar al volver la red.
5. Rotación y cambio de tema (claro/oscuro) con datos cargados: no repite la petición y conserva la posición.
6. "Cómo llegar" con y sin app de mapas instalada; "Copiar dirección".
7. "Limpiar filtros" restablece municipio y texto y vuelve a consultar.
