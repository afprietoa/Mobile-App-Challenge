# Reto 3 — Tic-Tac-Toe

Juego de tres en raya para Android, escrito en Kotlin, basado en el tutorial
"Tic-Tac-Toe for Android" de Frank McCown (Harding University).

## Reglas

- El humano juega con **X** (verde) y el computador (Android) con **O** (rojo).
- En la primera partida empieza el humano; a partir de ahí, quién empieza
  alterna en cada partida nueva.
- Gana quien complete una fila, columna o diagonal. Si se llena el tablero
  sin un ganador, es empate.

## Cómo juega el computador

`TicTacToeGame.getComputerMove()` sigue esta prioridad:
1. Si el computador puede ganar en su siguiente jugada, la hace.
2. Si no, y el humano podría ganar en su siguiente jugada, el computador bloquea esa posición.
3. Si ninguna de las anteriores aplica, elige una posición libre al azar.

## Cómo jugar

- Toca una casilla libre para colocar tu X; el computador responde automáticamente.
- El marcador bajo el tablero lleva la cuenta de partidas ganadas por cada lado y empates.
- Usa el menú (⋮ arriba a la derecha) → **Nueva partida** para reiniciar el tablero en cualquier momento.

## Cómo correr las pruebas

```bash
./gradlew :app:test                 # TicTacToeGameTest (unitarias)
./gradlew :app:connectedAndroidTest # MainActivityTest (instrumentadas, requiere emulador/dispositivo)
```

## Notas de implementación

- Se usa `ViewBinding` para acceder a las vistas y un `MenuProvider` para el menú
  "Nueva partida" (`onCreateOptionsMenu`/`onOptionsItemSelected` están deprecados).
- AGP 9.4.0 integra soporte de Kotlin de forma nativa en `com.android.application`;
  no hace falta aplicar el plugin `org.jetbrains.kotlin.android` por separado
  (aplicarlo además falla con `Cannot add extension with name 'kotlin'`).
