package com.example.trekly.composeable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogButtons(
    confirmLabel: String = "OK",
    dismissLabel: String = "Cancel",
    deleteLabel: String = "Delete",
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onDelete: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            if (onDelete != null) {
                TextButton(
                    onClick = onDelete
                ) {
                    Text(deleteLabel, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(dismissLabel)
            }
            TextButton(
                onClick = onConfirmation,
                enabled = confirmEnabled,
            ) {
                Text(confirmLabel)
            }
        }
    }
}