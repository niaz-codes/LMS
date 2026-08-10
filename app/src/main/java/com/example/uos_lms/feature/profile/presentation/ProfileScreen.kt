package com.example.uos_lms.feature.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.session.ThemeMode
import com.example.uos_lms.core.session.ThemeViewModel
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.AvatarCropDialog
import com.example.uos_lms.core.ui.components.ErrorState

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    var cropSourceUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> cropSourceUri = uri }
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        bottomBar = bottomBar,
    ) { padding ->
        val user = uiState.user

        if (uiState.isLoading || user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::clearError)
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { -it / 3 },
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    )
                    .statusBarsPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !uiState.isUploadingPhoto) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (user.profilePhotoUrl != null) {
                            AsyncImage(
                                model = user.profilePhotoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(96.dp),
                            )
                        }
                        if (uiState.isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                val progress = uiState.uploadProgress
                                if (progress != null) {
                                    CircularProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp,
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change profile photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        user.role.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                    if (user.profilePhotoUrl != null) {
                        TextButton(
                            onClick = viewModel::removePhoto,
                            enabled = !uiState.isUploadingPhoto,
                        ) {
                            Text("Remove Photo", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileDetailRow("Email", user.email)
                        ProfileDetailRow("Phone", user.phone)
                        ProfileDetailRow("Father Name", user.fatherName)
                    }
                }

                OutlinedButton(
                    onClick = { showEditProfile = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Edit Profile")
                }

                OutlinedButton(
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Change Password")
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = themeMode == mode,
                                    onClick = { themeViewModel.setThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                                ) {
                                    Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        "Logout",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }

    val currentUser = uiState.user
    if (showEditProfile && currentUser != null) {
        EditProfileDialog(
            user = currentUser,
            isSaving = uiState.isSavingProfile,
            onDismiss = { showEditProfile = false },
            onSave = { fullName, fatherName, phone, cnic ->
                viewModel.updateProfile(fullName, fatherName, phone, cnic)
                showEditProfile = false
            },
        )
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            isSaving = uiState.isChangingPassword,
            onDismiss = { showChangePassword = false },
            onSave = { oldPassword, newPassword ->
                viewModel.changePassword(oldPassword, newPassword)
                showChangePassword = false
            },
        )
    }

    cropSourceUri?.let { sourceUri ->
        AvatarCropDialog(
            sourceUri = sourceUri,
            onDismiss = { cropSourceUri = null },
            onCropConfirmed = { croppedUri ->
                cropSourceUri = null
                viewModel.updatePhoto(croppedUri)
            },
        )
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (fullName: String, fatherName: String, phone: String, cnic: String) -> Unit,
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var fatherName by remember { mutableStateOf(user.fatherName) }
    var phone by remember { mutableStateOf(user.phone) }
    var cnic by remember { mutableStateOf(user.cnic) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                AppTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = fatherName, onValueChange = { fatherName = it }, label = "Father Name", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = phone, onValueChange = { phone = it }, label = "Phone", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = cnic, onValueChange = { cnic = it }, label = "CNIC", modifier = Modifier.fillMaxWidth())
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (fullName.isBlank()) {
                        error = "Full name is required"
                        return@TextButton
                    }
                    onSave(fullName, fatherName, phone, cnic)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ChangePasswordDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (oldPassword: String, newPassword: String) -> Unit,
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                AppTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = "Current Password",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New Password",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm New Password",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                        error = "Both password fields are required"
                        return@TextButton
                    }
                    if (newPassword != confirmPassword) {
                        error = "New passwords do not match"
                        return@TextButton
                    }
                    if (newPassword.length < 6) {
                        error = "New password must be at least 6 characters"
                        return@TextButton
                    }
                    onSave(oldPassword, newPassword)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
