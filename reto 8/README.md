# Reto 8 — Directorio de empresas de software con SQLite (Kotlin)

App Android que gestiona un directorio de empresas de desarrollo de software guardado en una base de datos **SQLite** local, usando `SQLiteOpenHelper` (sin Room), tal como pide el reto.

## Qué hace

- **CRUD** completo de empresas: crear, listar, editar y eliminar.
- Cada empresa guarda: nombre, página web, teléfono, email, productos y servicios, y clasificación (**consultoría**, **desarrollo a la medida** y/o **fábrica de software**; puede tener varias).
- **Listado** ordenado alfabéticamente, con **filtro por nombre** (contiene, sin distinguir mayúsculas) y por **clasificación** (chips). Los filtros se combinan.
- **Confirmación** antes de eliminar (desde la lista y desde la edición).
- Validación del formulario: nombre y al menos una clasificación obligatorios; email, teléfono y web con formato válido si se llenan. La URL se normaliza a `https://…`.
- Al editar, botones para **llamar**, **escribir** y **abrir la web** de la empresa.

## Base de datos

```sql
CREATE TABLE companies (
    _id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT    NOT NULL,
    website        TEXT    NOT NULL DEFAULT '',
    phone          TEXT    NOT NULL DEFAULT '',
    email          TEXT    NOT NULL DEFAULT '',
    products       TEXT    NOT NULL DEFAULT '',
    classification INTEGER NOT NULL DEFAULT 0
);
```

La clasificación es una **máscara de bits** (`1` consultoría, `2` a la medida, `4` fábrica), así una empresa puede tener varias sin tabla intermedia. Filtrar es `(classification & ?) != 0`. Todas las consultas usan parámetros `?`, nunca concatenan texto del usuario.

Consulta de búsqueda (la cláusula `WHERE` solo incluye los filtros activos):

```sql
SELECT * FROM companies
WHERE name LIKE ? ESCAPE '\'      -- texto escapado: % y _ se buscan literalmente
  AND (classification & ?) != 0   -- OR entre las clasificaciones elegidas
ORDER BY name COLLATE NOCASE ASC;
```

## Estructura

```
app/src/main/java/com/unal/reto8/
├── model/   Company, Classification (máscara), CompanyValidator   # lógica pura
├── data/    CompanyDbHelper, CompanyRepository (+CompanyFilter), SqliteCompanyRepository, LikeEscaper
└── ui/      CompanyListActivity (launcher), CompanyAdapter, CompanyFormActivity, DeleteConfirmation
```

Las Activities solo conocen la interfaz `CompanyRepository`. El acceso a la BD se hace fuera del hilo principal con un `Executor` de un solo hilo.

## Pruebas

```
./gradlew :app:testDebugUnitTest                      # unitarias (sin dispositivo)
./gradlew :app:connectedDebugAndroidTest              # repositorio SQLite + UI con Espresso (requiere dispositivo/emulador)
```

Ver `TESTING.md` para el detalle y la lista de verificación manual.

## Limitaciones conocidas

- Sin Room, sin sincronización en la nube ni importar/exportar.
- Con varios chips activos se muestran las empresas con **cualquiera** de las clasificaciones elegidas (OR); entre nombre y clasificación se exige ambos (AND).
- `COLLATE NOCASE` y `LIKE` solo ignoran mayúsculas ASCII: "Ágil" y "agil" no coinciden.
- `onUpgrade` está vacío (esquema v1). Un cambio de esquema debe subir `DB_VERSION` y migrar con `ALTER TABLE`, no recrear la tabla.
- La clasificación se exige (≥ 1) porque el enunciado no aclara si es opcional; se puede relajar en `CompanyValidator`.
