# Reto 6 — Cambio de orientación y persistencia de estado

Continuación del Reto 5 (tres en raya con vista personalizada y sonido), basada en el
tutorial "Changing the Orientation and Saving State" de Frank McCown (Harding University).

## Novedades

- **Layout horizontal** (`res/layout-land/activity_main.xml`): tablero de 270 dp a la
  izquierda; mensaje de turno y marcadores a la derecha.
- **Estado de la partida al rotar**: `onSaveInstanceState` guarda tablero, fin de juego,
  turno del computador, quién empieza, sonido y mensaje; `onCreate` los restaura.
  Guardar el turno evita el bug de "el computador mueve de más" tras rotar.
- **Marcadores persistentes**: `SharedPreferences` (`ttt_prefs`), escritos en `onStop()`.
- **Dificultad persistente** (reto extra 1): se guarda el `ordinal` del enum.
- **Rotar antes de la jugada del computador** (reto extra 2): la Activity nueva retoma el
  turno pendiente en `onResume`; no hay crash porque el `Handler` se cancela en `onPause`.
- **Menú**: Nueva partida · Dificultad · Sonido · Reiniciar marcador · Acerca de
  (se eliminó "Salir").

## Dónde se guarda cada cosa

| Dato | Mecanismo | Sobrevive a |
|---|---|---|
| Tablero, turno, mensaje, quién empieza, sonido | `Bundle` (`onSaveInstanceState`) | Rotación |
| Marcadores, dificultad | `SharedPreferences` (`onStop`) | Rotación y cierre de la app |

## Cómo correr las pruebas

```bash
./gradlew :app:test                 # TicTacToeGameTest (unitarias)
./gradlew :app:connectedAndroidTest # MainActivityTest (instrumentadas, requiere emulador/dispositivo)
```

## Notas de implementación

- Restaurar en `onCreate` equivale a hacerlo en `onRestoreInstanceState`; se eligió
  `onCreate` para tener un único `if (savedInstanceState == null) … else …`.
- El tema ya es `NoActionBar` con una `Toolbar` propia, por lo que no se aplica
  `Theme.NoTitleBar` del tutorial original.
