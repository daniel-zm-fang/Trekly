package com.example.trekly.composeable

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Wrapper around TextField composable to make it handle onClick events.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableTextField(value: String, label: String, leadingIcon: (@Composable () -> Unit)? = null, modifier: Modifier, onClick: () -> Unit) {
    TextField(
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
        colors = TextFieldDefaults.textFieldColors(
            disabledTextColor = LocalContentColor.current.copy(),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(),
            disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(),
        ),
        label = { Text(label) }
    )
}
