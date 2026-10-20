# Pruebas — Reto 8 (Directorio de empresas con SQLite)

## Automáticas

| Comando | Qué cubre | Necesita dispositivo |
|---|---|---|
| `./gradlew :app:testDebugUnitTest` | `ClassificationTest`, `CompanyValidatorTest`, `LikeEscaperTest` | No |
| `./gradlew :app:connectedDebugAndroidTest --tests "*SqliteCompanyRepositoryTest"` | CRUD y búsqueda sobre una BD en memoria | Sí |
| `./gradlew :app:connectedDebugAndroidTest --tests "*CompanyListActivityTest"` | UI: lista vacía, listado, eliminar (cancelar/confirmar), filtros, crear desde el botón + | Sí |

`CompanyListActivityTest` borra `companies.db` antes y después de cada test: **no lo ejecutes en un dispositivo con datos que quieras conservar**.

## Lista de verificación manual

### CRUD
- [ ] Crear una empresa con todos los campos y las 3 clasificaciones; aparece en la lista.
- [ ] Crear una empresa solo con nombre y una clasificación (campos opcionales vacíos).
- [ ] Guardar con nombre vacío → error "Obligatorio" y no se guarda.
- [ ] Guardar sin clasificación → aparece "Elige al menos una clasificación" y no se guarda.
- [ ] Email `abc` y teléfono `123` → "Formato no válido".
- [ ] URL `unal.edu.co` se guarda como `https://unal.edu.co`.
- [ ] Tocar una empresa abre el formulario relleno; editar y guardar actualiza la lista.
- [ ] Papelera de la lista: el diálogo muestra el nombre; **Cancelar** no borra; **Eliminar** borra.
- [ ] Menú "Eliminar" del formulario de edición: mismo diálogo; no aparece al crear una empresa nueva.

### Filtros
- [ ] Buscar `alf` encuentra "Alfa" y "MALFA" (sin distinguir mayúsculas).
- [ ] Un chip muestra las empresas con esa clasificación, incluso si tienen otras.
- [ ] Dos chips muestran las que tengan cualquiera de las dos.
- [ ] Nombre + chip aplican ambos.
- [ ] Buscar `%` o `_` no devuelve todas las empresas.
- [ ] Sin resultados → "Ninguna empresa coincide con el filtro."

### Datos y robustez
- [ ] `O'Reilly`, tildes, ñ y emojis se guardan y se ven bien.
- [ ] Texto largo en "Productos y servicios" (el formulario se desplaza).
- [ ] Rotar la lista conserva búsqueda y chips; rotar el formulario conserva lo escrito.
- [ ] Forzar cierre (`adb shell am force-stop com.unal.reto8`) y reabrir: los datos persisten.
- [ ] Instalar encima de una versión anterior no borra los datos.
- [ ] Con 200+ empresas la lista y la búsqueda siguen fluidas.

### Contacto (Fase 7)
- [ ] Con teléfono: "Llamar" abre el marcador con el número.
- [ ] Con email: "Escribir" abre el cliente de correo.
- [ ] Con web: "Abrir web" abre el navegador.
- [ ] Sin esos datos los botones no aparecen; en modo "nueva empresa" tampoco.
- [ ] Modo oscuro legible en lista, formulario y diálogo.

## Inspeccionar la BD
Android Studio → *App Inspection* → *Database Inspector* → `companies.db` → tabla `companies`.
