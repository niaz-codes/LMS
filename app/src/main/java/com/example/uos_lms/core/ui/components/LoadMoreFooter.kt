package com.example.uos_lms.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Client-side "load more" chunking footer — this app has no server-side
 * pagination infrastructure (every list screen loads a fully-filtered
 * snapshot into memory), so this reveals more of the already-fetched list
 * rather than issuing a new query.
 */
@Composable
fun LoadMoreFooter(remaining: Int, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        OutlinedButton(onClick = onLoadMore, modifier = Modifier.align(Alignment.Center)) {
            Text("Load $remaining more")
        }
    }
}
