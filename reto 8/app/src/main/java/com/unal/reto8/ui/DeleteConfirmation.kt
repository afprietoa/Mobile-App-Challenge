package com.unal.reto8.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.unal.reto8.R
import com.unal.reto8.model.Company

/** Diálogo de confirmación compartido por la lista y el formulario. */
object DeleteConfirmation {
    fun show(context: Context, company: Company, onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_title)
            .setMessage(context.getString(R.string.delete_message, company.name))
            .setPositiveButton(R.string.delete_confirm) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
