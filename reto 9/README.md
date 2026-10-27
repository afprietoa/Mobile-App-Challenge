# Reto 9 — Accediendo al GPS (Puntos cercanos)

App Android (Kotlin) que toma la ubicación actual del usuario (GPS) o una dirección escrita, y dibuja en un mapa los puntos de interés (hospitales, lugares turísticos, etc.) dentro de un radio en km configurable en **Preferencias**. Cada marcador muestra el **nombre** del lugar, su categoría y la distancia.

## Cómo funciona

- **Mapa:** [osmdroid](https://github.com/osmdroid/osmdroid) (OpenStreetMap, sin API key).
- **Ubicación:** `FusedLocationProviderClient` con permiso en tiempo de ejecución; la dirección se obtiene con `Geocoder`.
- **Puntos de interés:** consulta a la [Overpass API](https://overpass-api.de) (`around:<metros>,<lat>,<lon>`), con `HttpURLConnection` + `org.json`.
- **Preferencias:** radio (1–20 km, por defecto 3) y categorías (por defecto hospital + lugar turístico).
- **Extras:** menú de búsqueda de dirección como origen alterno, pulsación larga sobre el mapa para fijar un origen manual, y botón "Mi ubicación" para volver al GPS.

## Estructura

```
com.unal.reto9
├── model/    PointOfInterest, PoiCategory, GeoMath (haversine), RadiusPolicy
├── data/     OverpassQueryBuilder, OverpassParser, PoiRepository(+Overpass), SettingsStore, ServiceLocator
├── location/ LocationSource / LocationProvider (Fused)
└── ui/       MapActivity, MapViewModel, PoiMarkers, SettingsActivity
```

La lógica pura (distancia, radio, consulta, parseo) tiene tests JUnit. `ServiceLocator` permite sustituir red y GPS por *fakes* en los tests instrumentados.

## Probar

- Unitarias: `./gradlew :app:testDebugUnitTest`
- Instrumentadas (emulador/dispositivo): `./gradlew :app:connectedDebugAndroidTest`
- En el emulador usa una imagen **con Google Play** y fija la ubicación en *Extended controls → Location*; si no, `getCurrentLocation` devuelve `null`.
- Si Gradle falla con `jlink executable ... does not exist`, apunta a un JDK completo (p. ej. `-Dorg.gradle.java.installations.paths=<JDK 25>`).

## Limitaciones

Depende de un servicio público gratuito con límite de uso (puede responder 429/504), máximo 100 resultados, sin caché offline, un único icono para todas las categorías.
