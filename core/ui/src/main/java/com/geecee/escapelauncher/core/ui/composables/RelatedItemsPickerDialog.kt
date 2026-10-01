package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.geecee.escapelauncher.core.model.LauncherItem

/**
 * Full-screen picker for configuring [owner]'s "related items" - other apps/shortcuts shown as
 * quick-launch entries in [owner]'s own long-press popup. A plain full-screen [Dialog] rather
 * than a nav destination, since the bottom sheet's callers have no navigation backstack of
 * their own to push onto.
 */
@Composable
fun RelatedItemsPickerDialog(
    owner: LauncherItem,
    items: List<LauncherItem>,
    selectedItems: List<LauncherItem>,
    onDismiss: () -> Unit,
    onItemSelected: (item: LauncherItem, isSelected: Boolean) -> Unit,
    onItemMoved: (fromIndex: Int, toIndex: Int) -> Unit,
    title: String
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ReorderableSelectionLazyColumn(
                items = items.filter { it.itemKey != owner.itemKey },
                selectedItems = selectedItems,
                id = { it.itemKey },
                label = { it.displayName },
                title = title,
                reorderEnabled = true,
                onBackClicked = onDismiss,
                onItemSelected = onItemSelected,
                onItemMoved = onItemMoved,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}
