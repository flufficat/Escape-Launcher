package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.geecee.escapelauncher.core.model.RenameTarget
import com.geecee.escapelauncher.core.ui.R

/**
 * Rename dialog for an app or shortcut. Saving with a blank name clears the rename override,
 * reverting to the item's real/published label.
 */
@Composable
fun RenameDialog(
    target: RenameTarget,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(target) {
        mutableStateOf(TextFieldValue(target.currentLabel, selection = androidx.compose.ui.text.TextRange(target.currentLabel.length)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.text) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
