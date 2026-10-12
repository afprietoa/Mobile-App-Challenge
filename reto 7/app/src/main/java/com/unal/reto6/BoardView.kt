package com.unal.reto6

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

/**
 * Tablero de tres en raya dibujado a mano: una cuadrícula 3x3 más una imagen por
 * cada casilla ocupada en [game]. Solo dibuja; la Activity decide qué pasa con los toques.
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val humanBitmap: Bitmap = loadBitmap(R.drawable.x_img)
    private val computerBitmap: Bitmap = loadBitmap(R.drawable.o_img)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.board_line)
        strokeWidth = GRID_WIDTH.toFloat()
    }

    // Se reutiliza en cada onDraw para no asignar objetos durante el dibujo.
    private val destRect = Rect()

    /** Estado que se pinta. Asignarlo (o cambiarlo) redibuja el tablero. */
    var game: TicTacToeGame? = null
        set(value) {
            field = value
            invalidate()
        }

    /** Ancho de una celda en píxeles; sirve para convertir un toque (x) en columna. */
    val boardCellWidth: Int get() = width / 3

    /** Alto de una celda en píxeles; sirve para convertir un toque (y) en fila. */
    val boardCellHeight: Int get() = height / 3

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.board_background))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val boardWidth = width.toFloat()
        val boardHeight = height.toFloat()
        val cellWidth = boardCellWidth.toFloat()
        val cellHeight = boardCellHeight.toFloat()

        // Dos líneas verticales
        canvas.drawLine(cellWidth, 0f, cellWidth, boardHeight, paint)
        canvas.drawLine(cellWidth * 2, 0f, cellWidth * 2, boardHeight, paint)

        // Dos líneas horizontales
        canvas.drawLine(0f, cellHeight, boardWidth, cellHeight, paint)
        canvas.drawLine(0f, cellHeight * 2, boardWidth, cellHeight * 2, paint)

        // Una imagen por casilla ocupada, encogida al tamaño de la celda
        val currentGame = game ?: return
        val inset = GRID_WIDTH / 2
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val col = i % 3
            val row = i / 3

            destRect.set(
                col * boardCellWidth + inset,
                row * boardCellHeight + inset,
                (col + 1) * boardCellWidth - inset,
                (row + 1) * boardCellHeight - inset
            )

            when (currentGame.getBoardOccupant(i)) {
                TicTacToeGame.HUMAN_PLAYER -> canvas.drawBitmap(humanBitmap, null, destRect, null)
                TicTacToeGame.COMPUTER_PLAYER -> canvas.drawBitmap(computerBitmap, null, destRect, null)
            }
        }
    }

    // BitmapFactory.decodeResource no soporta VectorDrawable; toBitmap() sí.
    private fun loadBitmap(drawableRes: Int): Bitmap =
        checkNotNull(ContextCompat.getDrawable(context, drawableRes)).toBitmap()

    companion object {
        /** Grosor de las líneas de la cuadrícula, en píxeles. */
        const val GRID_WIDTH = 6
    }
}
