package com.example.uos_lms.feature.auth.presentation.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.ui.components.AnimatedAuthLogo
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.FloatingOrbsBackground
import com.example.uos_lms.core.ui.components.GlassCard
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.ProfilePhotoPicker
import com.example.uos_lms.core.ui.components.RoleSelectorRow
import com.example.uos_lms.core.ui.components.glassTextFieldColors
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(Unit) {
        viewModel.registrationSubmitted.collect { onRegistered() }
    }

    val (startTarget, endTarget) = roleGradientColors(uiState.role)
    val animatedStart by animateColorAsState(
        targetValue = startTarget,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "gradientStart",
    )
    val animatedEnd by animateColorAsState(
        targetValue = endTarget,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "gradientEnd",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(animatedStart, animatedEnd))),
    ) {
        FloatingOrbsBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedAuthLogo(visibleState = visibleState, logoSize = 76.dp)

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(500, delayMillis = 150)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Your account will be reviewed by the Admin before you can log in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(500, delayMillis = 250)) +
                    slideInVertically(tween(500, delayMillis = 250)) { it / 3 },
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "I am registering as",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    RoleSelectorRow(selected = uiState.role, onSelect = viewModel::onRoleChange)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(550, delayMillis = 350)) +
                    slideInVertically(tween(550, delayMillis = 350)) { it / 3 } +
                    scaleIn(tween(550, delayMillis = 350), initialScale = 0.94f),
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        SectionHeader(icon = Icons.Filled.PhotoCamera, title = "Profile Photo (Optional)")
                        Spacer(modifier = Modifier.height(14.dp))
                        ProfilePhotoPicker(
                            localUri = uiState.photoUri,
                            remoteUrl = null,
                            onPhotoPicked = viewModel::onPhotoPicked,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )

                        SectionDivider()

                        SectionHeader(icon = Icons.Filled.Person, title = "Personal Information")
                        Spacer(modifier = Modifier.height(14.dp))
                        AppTextField(
                            value = uiState.fullName,
                            onValueChange = viewModel::onFullNameChange,
                            label = "Full Name",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.fullNameError,
                            leadingIcon = Icons.Filled.Person,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.fatherName,
                            onValueChange = viewModel::onFatherNameChange,
                            label = "Father Name",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.fatherNameError,
                            leadingIcon = Icons.Filled.Badge,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.cnic,
                            onValueChange = viewModel::onCnicChange,
                            label = "CNIC (#####-#######-#)",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.cnicError,
                            keyboardType = KeyboardType.Number,
                            leadingIcon = Icons.Filled.Fingerprint,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            label = "Phone Number (03XXXXXXXXX)",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.phoneError,
                            keyboardType = KeyboardType.Phone,
                            leadingIcon = Icons.Filled.Phone,
                            colors = glassTextFieldColors(),
                        )

                        SectionDivider()

                        SectionHeader(icon = Icons.Filled.Lock, title = "Account Credentials")
                        Spacer(modifier = Modifier.height(14.dp))
                        AppTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = "Email Address",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.emailError,
                            keyboardType = KeyboardType.Email,
                            leadingIcon = Icons.Filled.Email,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = "Password",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.passwordError,
                            isPassword = true,
                            leadingIcon = Icons.Filled.Lock,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = "Confirm Password",
                            modifier = Modifier.fillMaxWidth(),
                            errorMessage = uiState.confirmPasswordError,
                            isPassword = true,
                            leadingIcon = Icons.Filled.LockReset,
                            colors = glassTextFieldColors(),
                        )

                        AnimatedVisibility(
                            visible = uiState.uploadProgress != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Uploading photo… ${uiState.uploadProgress ?: 0}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (uiState.uploadProgress ?: 0) / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = uiState.submitError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text(uiState.submitError.orEmpty(), color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        LoadingButton(
                            text = "Create Account",
                            onClick = viewModel::submit,
                            isLoading = uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            containerGradient = Brush.horizontalGradient(listOf(animatedStart, animatedEnd)),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                            Text("Already have an account? Login")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    Spacer(modifier = Modifier.height(20.dp))
}
