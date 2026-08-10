package com.example.uos_lms.core.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfilePhotoPicker(
    localUri: Uri?,
    remoteUrl: String?,
    onPhotoPicked: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> cropSourceUri = uri }

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val model = localUri ?: remoteUrl
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            )
        } else {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Pick profile photo")
        }
    }

    cropSourceUri?.let { sourceUri ->
        AvatarCropDialog(
            sourceUri = sourceUri,
            onDismiss = { cropSourceUri = null },
            onCropConfirmed = { croppedUri ->
                cropSourceUri = null
                onPhotoPicked(croppedUri)
            },
        )
    }
}
