package com.example.uos_lms.core.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/** Lets the user pan and pinch-zoom a photo into a square crop that is previewed as an avatar. */
@Composable
fun AvatarCropDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onCropConfirmed: (Uri) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bitmap = remember(sourceUri) {
        context.contentResolver.openInputStream(sourceUri)?.use(BitmapFactory::decodeStream)
    }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var cropSize by remember { mutableStateOf(IntSize.Zero) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Crop profile photo", style = MaterialTheme.typography.titleLarge)
                Text("Pinch to zoom and drag to position your avatar.", style = MaterialTheme.typography.bodyMedium)

                if (bitmap == null) {
                    Text("This image could not be opened.", color = MaterialTheme.colorScheme.error)
                } else {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .onSizeChanged { cropSize = it }
                            .pointerInput(bitmap, cropSize) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (zoom * gestureZoom).coerceIn(1f, 5f)
                                    offset = constrainedOffset(bitmap, cropSize, newZoom, offset + pan)
                                    zoom = newZoom
                                }
                            },
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (cropSize.width > 0) {
                                val base = max(size.width / bitmap.width, size.height / bitmap.height)
                                val width = (bitmap.width * base * zoom).toInt()
                                val height = (bitmap.height * base * zoom).toInt()
                                drawImage(
                                    image = bitmap.asImageBitmap(),
                                    dstOffset = androidx.compose.ui.unit.IntOffset(
                                        ((size.width - width) / 2f + offset.x).toInt(),
                                        ((size.height - height) / 2f + offset.y).toInt(),
                                    ),
                                    dstSize = IntSize(width, height),
                                )
                            }
                        }
                    }
                    Text("Live circular preview", style = MaterialTheme.typography.labelMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        enabled = bitmap != null && cropSize.width > 0,
                        onClick = {
                            val croppedUri = bitmap?.let { cropAndWriteAvatar(context.cacheDir, it, cropSize, zoom, offset) }
                            if (croppedUri != null) onCropConfirmed(croppedUri)
                        },
                    ) { Text("Use photo") }
                }
            }
        }
    }
}

private fun constrainedOffset(bitmap: Bitmap, cropSize: IntSize, zoom: Float, requested: Offset): Offset {
    if (cropSize.width == 0 || cropSize.height == 0) return requested
    val base = max(cropSize.width.toFloat() / bitmap.width, cropSize.height.toFloat() / bitmap.height)
    val maxX = max(0f, (bitmap.width * base * zoom - cropSize.width) / 2f)
    val maxY = max(0f, (bitmap.height * base * zoom - cropSize.height) / 2f)
    return Offset(requested.x.coerceIn(-maxX, maxX), requested.y.coerceIn(-maxY, maxY))
}

private fun cropAndWriteAvatar(cacheDir: File, bitmap: Bitmap, cropSize: IntSize, zoom: Float, offset: Offset): Uri? = runCatching {
    val base = max(cropSize.width.toFloat() / bitmap.width, cropSize.height.toFloat() / bitmap.height)
    val cropWidth = min(bitmap.width, (cropSize.width / (base * zoom)).toInt())
    val cropHeight = min(bitmap.height, (cropSize.height / (base * zoom)).toInt())
    val left = ((bitmap.width - cropWidth) / 2f - offset.x / (base * zoom)).toInt().coerceIn(0, bitmap.width - cropWidth)
    val top = ((bitmap.height - cropHeight) / 2f - offset.y / (base * zoom)).toInt().coerceIn(0, bitmap.height - cropHeight)
    val cropped = Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
    val square = Bitmap.createScaledBitmap(cropped, 512, 512, true)
    val output = File(cacheDir, "avatar_crop_${System.currentTimeMillis()}.jpg")
    FileOutputStream(output).use { square.compress(Bitmap.CompressFormat.JPEG, 92, it) }
    if (cropped != square) cropped.recycle()
    square.recycle()
    Uri.fromFile(output)
}.getOrNull()
