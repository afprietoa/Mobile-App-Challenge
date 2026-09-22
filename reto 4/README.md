# Reto 4 — Menús y Dialog Boxes

Continuación del Reto 3 (tres en raya): añade un menú de opciones completo y
varios cuadros de diálogo, basado en el tutorial "Menus and Dialog Boxes" de
Frank McCown (Harding University).

## Menú

- **Nueva partida** — reinicia el tablero, igual que en el Reto 3.
- **Dificultad** — abre un diálogo con radio buttons (Fácil / Difícil / Experto)
  para cambiar el nivel de la IA en cualquier momento.
- **Acerca de** — muestra un diálogo con el ícono de la app y una breve
  descripción del juego.
- **Salir** — pide confirmación (Sí/No) antes de cerrar la app.

## Niveles de dificultad

`TicTacToeGame.getComputerMove()` según `difficultyLevel`:
- **Fácil**: siempre mueve al azar.
- **Difícil**: gana si puede; si no, mueve al azar (no bloquea al humano).
- **Experto** (por defecto): gana si puede; si no, bloquea al humano; si no,
  mueve al azar.

## Cómo jugar

- Toca una casilla libre para colocar tu X; el computador responde automáticamente.
- El marcador bajo el tablero lleva la cuenta de partidas ganadas por cada lado y empates.
- Usa el menú (⋮ arriba a la derecha) para cambiar la dificultad, ver "Acerca de",
  empezar una nueva partida o salir.

## Cómo correr las pruebas

```bash
./gradlew :app:test                 # TicTacToeGameTest (unitarias)
./gradlew :app:connectedAndroidTest # MainActivityTest (instrumentadas, requiere emulador/dispositivo)
```

## Notas de implementación

- Los diálogos se construyen con `AlertDialog.Builder` directamente desde el
  `MenuProvider`, no con `Activity.onCreateDialog(int)`/`showDialog(int)`
  (deprecados) del tutorial original.
- El ícono de la app es un adaptive icon vectorial (grid + X/O) en vez de un
  `icon.png` estático.
