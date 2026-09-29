package com.unal.reto5

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Tablero de tres en raya dibujado a mano: una cuadrícula 3x3.
 * Solo dibuja; la Activity decide qué pasa con los toques.
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.board_line)
        strokeWidth = GRID_WIDTH.toFloat()
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
    }

    companion object {
        /** Grosor de las líneas de la cuadrícula, en píxeles. */
        const val GRID_WIDTH = 6
    }
}
