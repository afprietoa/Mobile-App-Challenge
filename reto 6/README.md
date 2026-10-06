# reto 6 — Gráficos y Sonido

Continuación del Reto 4 (tres en raya con menús y diálogos), basada en el tutorial
"Graphics and Sound" de Frank McCown (Harding University): el tablero deja de ser
una cuadrícula de botones y pasa a ser una vista personalizada que dibuja con Canvas,
con efectos de sonido y una pausa antes de que responda el computador.

## Novedades

- **`BoardView`**: vista propia que dibuja la cuadrícula (`Canvas.drawLine`) y las
  imágenes de X y O (`Canvas.drawBitmap`) según el estado de `TicTacToeGame`.
- **Toques**: un `OnTouchListener` convierte la coordenada tocada en una casilla.
- **Sonido**: `MediaPlayer` reproduce un efecto al mover el humano y otro al mover
  Android. Se cargan en `onResume` y se liberan en `onPause`.
  Menú ⋮ → **Sonido** activa/desactiva los efectos.
- **Pausa del computador**: Android espera 1 s (`Handler.postDelayed`) para que se vea
  el mensaje "Turno de Android." y no se solapen los sonidos; mientras espera, los
  toques del humano se ignoran.

## Menú

Nueva partida · Dificultad · Sonido · Acerca de · Salir

## Recursos

- `res/drawable/x_img.xml`, `o_img.xml`: fichas como vector drawables.
- `res/raw/human_move.wav`, `computer_move.wav`: efectos de sonido de relleno
  (tonos cortos generados). Para usar otros, reemplázalos por archivos con el mismo
  nombre base (p. ej. `human_move.mp3`) y borra el `.wav` correspondiente.

## Cómo correr las pruebas

```bash
./gradlew :app:test                 # TicTacToeGameTest (unitarias)
./gradlew :app:connectedAndroidTest # MainActivityTest (instrumentadas, requiere emulador/dispositivo)
```

## Notas de implementación

- Las fichas son vectores que se convierten a `Bitmap` con `toBitmap()`, porque
  `BitmapFactory.decodeResource` no decodifica `VectorDrawable`.
- `BoardView` usa un único constructor con `@JvmOverloads` en lugar de los tres
  constructores Java del tutorial.
