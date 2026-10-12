# Reto 7 — Lista de verificación manual (dos dispositivos)

Prerrequisitos: dos dispositivos o emuladores con Internet, `app/google-services.json` en su
lugar, Anonymous Auth habilitado y las reglas de `database.rules.json` publicadas.
En esta lista **A** es el anfitrión (X) y **B** el invitado (O).

| # | Escenario | Resultado esperado | OK |
|---|---|---|---|
| 1 | Abrir la app en A y en B | Ambos ven el lobby "Triqui online"; la lista está vacía o con juegos ajenos. | ☐ |
| 2 | A pulsa *Crear juego* | A queda en "Esperando oponente…" con el tablero vacío y bloqueado (tocarlo no hace nada). | ☐ |
| 3 | B ve el juego de A **sin refrescar** y lo toca | Ambos pasan a jugar; la línea de jugadores muestra `A (X) vs B (O)`; empieza A. | ☐ |
| 4 | Un tercer dispositivo C mira el lobby | El juego de A ya no aparece (está lleno). | ☐ |
| 5 | Jugar una partida hasta que gane A | A ve "¡Ganaste!", B ve "Perdiste."; el tablero queda bloqueado en ambos. | ☐ |
| 6 | Pulsar *Revancha* en cualquiera | Tablero vacío en ambos; empieza B (alterna). | ☐ |
| 7 | Jugar hasta que gane B | A ve "Perdiste.", B ve "¡Ganaste!". | ☐ |
| 8 | Nueva revancha y jugar hasta empate | Ambos ven "Empate.". | ☐ |
| 9 | Tocar fuera de turno | Toast "Espera tu turno."; el tablero no cambia. | ☐ |
| 10 | Tocar una casilla ocupada | Sin efecto. | ☐ |
| 11 | Rotar A durante la partida (vertical ↔ horizontal) | Tablero, turno y mensaje intactos en A; B no nota nada. | ☐ |
| 12 | B sale con *Atrás* en mitad de la partida y confirma | A ve "Tu oponente salió de la partida."; al aceptar vuelve al lobby; el nodo desaparece de la consola. | ☐ |
| 13 | A crea un juego, espera y sale con *Atrás* | El juego desaparece del lobby de los demás al instante. | ☐ |
| 14 | A y B en partida; activar modo avión en B | A ve "B se desconectó…" en pocos segundos; al reconectar B vuelve el turno. | ☐ |
| 15 | Matar la app de A mientras espera invitado | Su juego desaparece de la lista (`onDisconnect`). | ☐ |
| 16 | Dos dispositivos B y C tocan el mismo juego a la vez | Solo uno entra; el otro ve "No pudiste unirte: el juego ya no está disponible.". | ☐ |
| 17 | Cambiar el nombre desde el menú del lobby | Los juegos creados después muestran el nombre nuevo. | ☐ |
| 18 | *Jugar contra Android* | El juego del reto 6 funciona igual (dificultad, sonido, marcadores). | ☐ |
| 19 | Tema oscuro y modo horizontal en lobby y partida | Sin textos cortados ni solapamientos. | ☐ |
| 20 | Reglas: en el *Rules Playground* escribir en un juego `playing` con un `uid` ajeno | Escritura rechazada. | ☐ |

## Notas para la demo con el profesor

- Crear el juego **antes** de que el profesor abra el lobby y verificar que aparezca.
- Si el profesor llega primero, crear un juego propio y esperar: el lobby se actualiza solo.
- Tener a mano la consola de Firebase (*Realtime Database → Data*) para mostrar el nodo
  `games/{id}` cambiando en vivo con cada jugada.
