package com.yourfiles.manager.presentation.ui.components.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable confirmation dialog with a destructive (error-coloured) confirm button.
 *
 * @param title         Dialog title.
 * @param message       Body message.
 * @param confirmText   Label for the confirm button (defaults to "OK").
 * @param dismissText   Label for the dismiss button (defaults to "Cancel").
 * @param onConfirm     Called when the user taps the confirm button.
 * @param onDismiss     Called when the user taps the dismiss button or clicks outside.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}