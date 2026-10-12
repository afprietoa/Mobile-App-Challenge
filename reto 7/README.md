# Reto 7 — Triqui online

Continuación del Reto 6 (tres en raya con vista personalizada, sonido, rotación y
persistencia): ahora **dos jugadores juegan cada uno en su dispositivo**, mediados por
**Firebase Realtime Database**.

## Flujo

1. La app abre en el **lobby** con la lista, en tiempo real, de los juegos disponibles.
2. Un jugador **crea un juego** (queda en espera y su tablero está bloqueado) o **se une**
   a uno de la lista tocándolo. Solo cuando se une puede jugar.
3. Con dos jugadores la partida empieza: el anfitrión es **X** y el invitado **O**.
4. Cada jugada se valida en una transacción; ambos ven el tablero y el turno al instante.
5. Al terminar se puede pedir **revancha** (alterna quién empieza) o volver al lobby.
6. **Jugar contra Android** conserva el modo del reto 6.

## Requisitos del reto y dónde se cumplen

| Requisito del PDF | Implementación |
|---|---|
| En un juego hay 2 jugadores | `hostId` (X) y `guestId` (O); la unión es una transacción y las reglas impiden un tercero. |
| Cualquiera puede crear un juego | Botón *Crear juego* del lobby. |
| Lista de juegos disponibles al inicio | `LobbyActivity` es el launcher y escucha los juegos `waiting`. |
| Unirse a uno disponible o crear uno | Tocar un ítem de la lista, o *Crear juego*. |
| Solo juega quien se une | El tablero queda bloqueado mientras el estado no sea `playing`. |
| Servicio en la nube | Firebase Realtime Database + Authentication anónima. |

## Configuración de Firebase (una sola vez)

1. Crear un proyecto en la [consola de Firebase](https://console.firebase.google.com) y una
   **Realtime Database**.
2. Habilitar **Authentication → Sign-in method → Anónimo**.
3. Registrar la app Android con el paquete `com.unal.reto7` y colocar el
   `google-services.json` descargado en `app/`.
4. Publicar `database.rules.json` en *Realtime Database → Rules*.
5. Compilar con `./gradlew :app:assembleDebug` e instalar en dos dispositivos o emuladores.

Si la base de datos no está en `us-central1` y la app falla con "Firebase Database URL is
not configured", pasar la URL explícita a `FirebaseDatabase.getInstance("https://…")`.

## Estructura

| Capa | Archivos (`app/src/main/java/com/unal/reto7/`) |
|---|---|
| Lógica pura (con pruebas) | `online/OnlineGame.kt`, `online/OnlineGameRules.kt` |
| Acceso a datos | `online/GameRepository.kt`, `online/FirebaseGameRepository.kt` |
| UI online | `online/LobbyActivity.kt`, `online/GameListAdapter.kt`, `online/OnlineGameActivity.kt` |
| Preferencias | `online/PlayerPrefs.kt` (nombre del jugador) |
| Reutilizado del reto 6 | `BoardView.kt`, `TicTacToeGame.kt`, `MainActivity.kt` |

La UI solo conoce la interfaz `GameRepository`; ninguna Activity importa clases de Firebase.

## Modelo de datos

```
games/{gameId}
  hostId, hostName         # anfitrión (X)
  guestId, guestName       # invitado (O); "" hasta que alguien se une
  board                    # 9 caracteres: ' ', 'X', 'O'
  turn, firstMark          # de quién es el turno / quién abrió la partida actual
  status                   # waiting | playing | finished | abandoned
  result                   # 0 en curso, 1 empate, 2 gana X, 3 gana O
  createdAt                # ServerValue.TIMESTAMP
  hostOnline, guestOnline  # presencia (onDisconnect)
```

## Decisiones de diseño

- **El servidor es la fuente de verdad**: la Activity solo guarda el id del juego y se
  re-suscribe, por lo que rotar la pantalla no necesita `Bundle`.
- **Reglas como funciones puras** (`OnlineGameRules`): unirse, mover, revancha y abandonar
  son `OnlineGame -> OnlineGame?` y se ejecutan dentro de `runTransaction`, así dos
  dispositivos nunca se "ganan" la misma jugada.
- **Presencia** con `onDisconnect()`: si un jugador pierde la conexión, el otro lo ve.
- **Limpieza**: el anfitrión borra su juego en espera al salir o caerse; los juegos en
  espera de más de 10 minutos no se listan; quien cierra un juego terminado o abandonado
  lo borra.

## Pruebas

```bash
./gradlew :app:test                 # OnlineGameRulesTest + TicTacToeGameTest
./gradlew :app:connectedAndroidTest # MainActivityTest + LobbyActivityTest (emulador/dispositivo)
```

La lista de verificación para probar con dos dispositivos está en [`TESTING.md`](TESTING.md).

## Limitaciones conocidas

- Las reglas de Realtime Database solo restringen **quién** puede escribir y el formato de
  los campos; no validan que cada jugada sea legal. Esa validación ocurre en el cliente
  (transacción). Una Cloud Function la haría robusta frente a un cliente modificado.
- No hay reconexión a una partida tras cerrar la app: si un jugador cae, el rival debe salir.
- La identidad es anónima y el nombre es texto libre (máx. 16 caracteres).
- Los diálogos abiertos no sobreviven a una rotación (igual que en retos anteriores).
- Sonido y marcadores del reto 6 aplican solo al modo contra Android.
