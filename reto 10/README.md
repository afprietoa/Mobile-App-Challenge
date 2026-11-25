# Reto 10 — Consumiendo Webservices del Catálogo de Datos Colombiano

App Android (Kotlin) que consume el servicio web **"Puntos de recolección de residuos posconsumo (RAEE) – Norte de Santander"** del portal de datos abiertos `datos.gov.co` (API SODA/Socrata, id `c2fr-ezse`, 120 registros) y muestra la respuesta en una lista con filtros.

## Qué hace

- Envía parámetros al servicio desde la interfaz: **municipio**, **texto libre** (`$q`), paginación (`$limit`/`$offset`) y orden (`$order`).
- Presenta la respuesta en un `RecyclerView` (razón social, dirección, municipio, programa, residuos), con contador "Mostrando X de N", scroll infinito y pantalla de detalle (cómo llegar / copiar dirección).
- Parsea el JSON con `org.json` del SDK de Android; red con `HttpURLConnection` (sin librerías externas).
- Estados diferenciados: cargando, vacío y error (sin red, timeout, servidor, JSON inválido) con **Reintentar**.

## Ejemplo de petición

```
GET https://www.datos.gov.co/resource/c2fr-ezse.json
    ?$where=municipio='Ocaña'&$q=pilas&$order=raz_n_social&$limit=20&$offset=0
```

Respuesta (resumen): `[{"raz_n_social":"…","direccion":"…","municipio":"Ocaña","programa_de_recolecci_n":"…","tipo_de_residuos":"…","codigo_dane":"54498","departamento":"Norte de Santander"}, …]`

## Arquitectura (`com.unal.reto10`)

| Paquete | Contenido |
|---|---|
| `model` | `CollectionPoint`, `SodaFilter`, `LoadState`, `ErrorKind` |
| `data` | `SodaQueryBuilder` (URL), `CollectionPointParser` (JSON), `CollectionPointRepository` + `SodaCollectionPointRepository` (HTTP) + `FakeCollectionPointRepository` (tests), `ServiceLocator` |
| `ui` | `MainActivity`, `MainViewModel` (estado, paginación, descarte de respuestas obsoletas), `PointAdapter`, `DetailActivity` |

## Permisos

`INTERNET` y `ACCESS_NETWORK_STATE` (chequeo rápido de conectividad). Tráfico en claro deshabilitado.

## Ejecutar

- App: abrir en Android Studio o `./gradlew :app:installDebug` (minSdk 24).
- Tests: ver [`TESTING.md`](TESTING.md). Si Gradle falla con `jlink ... does not exist`, define `JAVA_HOME` con un JDK completo.

## Limitaciones

- Servicio público anónimo con límite de uso (puede responder 429 → "servicio no disponible").
- Sin caché offline; solo cubre el departamento Norte de Santander.
- `$q` busca por palabra completa (comportamiento de Socrata).
- El identificador del dataset puede cambiar en el portal: se centraliza en `data/Dataset.kt`.
