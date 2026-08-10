package com.example.uos_lms.feature.auth.presentation.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.ui.components.AnimatedAuthLogo
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.FloatingOrbsBackground
import com.example.uos_lms.core.ui.components.GlassCard
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.RoleSelectorRow
import com.example.uos_lms.core.ui.components.displayLabel
import com.example.uos_lms.core.ui.components.glassTextFieldColors
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun LoginScreen(
    onNavigate: (Routes) -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedRole = uiState.selectedRole
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { route -> onNavigate(route) }
    }

    val (startTarget, endTarget) = roleGradientColors(selectedRole)
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
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedAuthLogo(visibleState = visibleState)

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(500, delayMillis = 150)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "UNIVERSITY OF SHANGLA",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Learning Management System",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(500, delayMillis = 250)) +
                    slideInVertically(tween(500, delayMillis = 250)) { it / 3 },
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "I am signing in as",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    RoleSelectorRow(
                        selected = selectedRole,
                        onSelect = viewModel::onRoleChange,
                        roles = UserRole.LOGIN_ROLES,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(550, delayMillis = 350)) +
                    slideInVertically(tween(550, delayMillis = 350)) { it / 3 } +
                    scaleIn(tween(550, delayMillis = 350), initialScale = 0.94f),
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(PaddingValues(24.dp))) {
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        AnimatedContent(
                            targetState = selectedRole,
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                            label = "roleSubtitle",
                        ) { role ->
                            Text(
                                text = "Sign in as ${role.displayLabel()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        AppTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = "Email",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Email,
                            colors = glassTextFieldColors(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = "Password",
                            modifier = Modifier.fillMaxWidth(),
                            isPassword = true,
                            errorMessage = uiState.errorMessage,
                            colors = glassTextFieldColors(),
                        )
                        TextButton(
                            onClick = viewModel::onForgotPasswordClick,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Forgot Password?")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LoadingButton(
                            text = "Sign In",
                            onClick = viewModel::login,
                            isLoading = uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            containerGradient = Brush.horizontalGradient(listOf(animatedStart, animatedEnd)),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
                            Text("Don't have an account? Register")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.showForgotPasswordDialog) {
            ForgotPasswordDialog(
                email = uiState.forgotPasswordEmail,
                onEmailChange = viewModel::onForgotPasswordEmailChange,
                isSending = uiState.isSendingPasswordReset,
                errorMessage = uiState.forgotPasswordError,
                successMessage = uiState.forgotPasswordSuccessMessage,
                onSend = viewModel::sendPasswordReset,
                onDismiss = viewModel::onForgotPasswordDismiss,
            )
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    isSending: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forgot Password") },
        text = {
            if (successMessage != null) {
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
            } else {
                Column {
                    Text(
                        "Enter your registered email address and we'll send you a link to reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardType = KeyboardType.Email,
                        errorMessage = errorMessage,
                    )
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                TextButton(onClick = onDismiss) { Text("Done") }
            } else {
                LoadingButton(text = "Send Reset Link", isLoading = isSending, onClick = onSend)
            }
        },
        dismissButton = {
            if (successMessage == null) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
