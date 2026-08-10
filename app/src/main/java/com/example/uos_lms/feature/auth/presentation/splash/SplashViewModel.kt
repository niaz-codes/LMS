package com.example.uos_lms.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.navigation.toDashboardRoute
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _destination = Channel<Routes>(Channel.CONFLATED)
    val destination = _destination.receiveAsFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            val route = when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    when {
                        user == null -> Routes.Login
                        user.status == UserStatus.PENDING -> Routes.PendingApproval
                        user.status == UserStatus.REJECTED -> Routes.Rejected
                        user.status == UserStatus.SUSPENDED -> Routes.Suspended
                        else -> user.role.toDashboardRoute()
                    }
                }
                is AppResult.Error -> Routes.Login
            }
            _destination.send(route)
        }
    }
}
