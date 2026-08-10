package com.example.uos_lms.feature.admin.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.admin.domain.usecase.ObserveUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/** Feeds the live pending-approval count shown as a badge on the Admin nav bar's
 * Users tab — kept separate from AdminUserListViewModel since it's mounted on
 * every screen with a bottom nav bar, not just the Users tab itself (same
 * reasoning as NavAvatarViewModel for the Profile tab's avatar). */
@HiltViewModel
class AdminNavBadgeViewModel @Inject constructor(
    observeUsersUseCase: ObserveUsersUseCase,
) : ViewModel() {

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    init {
        observeUsersUseCase()
            .onEach { users -> _pendingCount.value = users.count { it.status == UserStatus.PENDING } }
            .launchIn(viewModelScope)
    }
}
