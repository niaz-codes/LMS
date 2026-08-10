package com.example.uos_lms.core.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/** Feeds the live profile-photo thumbnail shown on a nav bar's Profile tab —
 * kept separate from ProfileViewModel since it's mounted on every screen with
 * a bottom nav bar, not just the Profile screen itself. */
@HiltViewModel
class NavAvatarViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    init {
        observeCurrentUserUseCase()
            .onEach { user -> _photoUrl.value = user?.profilePhotoUrl }
            .launchIn(viewModelScope)
    }
}
