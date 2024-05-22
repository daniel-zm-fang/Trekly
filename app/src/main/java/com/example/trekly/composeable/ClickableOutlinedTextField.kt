package com.example.trekly.composeable

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Wrapper around OutlinedTextField composable to make it handle onClick events.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableOutlinedTextField(
    value: String,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier,
    onClick: () -> Unit,
    maxLines: Int = 1,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        leadingIcon = leadingIcon,
        onValueChange = {},
        enabled = false,
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = true,
            ),
        // Reset the colors from "enabled = false" above.
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor.copy(),
            disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedLabelColor.copy(),
            disabledBorderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor.copy(),
            disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(),
        ),
        label = { Text(label) },
        maxLines = maxLines,
        isError = isError,
    )
}
