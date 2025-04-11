package com.niksah.gagarin.utils.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Alert(
    showError: String,
    onDismissRequest: () -> Unit,
) {
    if (showError.isNotBlank()) {
        AlertDialog(
            onDismissRequest = onDismissRequest,// { showError = "" },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = onDismissRequest, //{ showError = "" }
                )
                {
                    Text(
                        "Ok",
                        color = Color.Black
                    )
                }
            },
            text = { Text(showError) })
    }
}