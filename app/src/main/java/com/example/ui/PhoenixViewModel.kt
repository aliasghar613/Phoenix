package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PhoenixPreferencesRepository
import com.example.data.PhoenixSettings
import com.example.ui.theme.ThemeMode
import com.example.util.NetworkMonitor
import com.example.util.UrlPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WebErrorState(
    val failedUrl: String,
    val errorCode: Int,
    val description: String,
    val isOffline: Boolean
)

data class PhoenixUiState(
    val currentUrl: String = UrlPolicy.BASE_URL,
    val pageTitle: String = "Phoenix",
    val loadProgress: Int = 0,
    val isLoading: Boolean = true,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isOnline: Boolean = true,
    val errorState: WebErrorState? = null,
    val showInitialSplash: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val settings: PhoenixSettings = PhoenixSettings()
)

class PhoenixViewModel(
    private val preferencesRepository: PhoenixPreferencesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PhoenixUiState(isOnline = networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<PhoenixUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { state ->
                    state.copy(isOnline = online)
                }
            }
        }

        viewModelScope.launch {
            delay(1800L)
            _uiState.update { it.copy(showInitialSplash = false) }
        }
    }

    fun onPageStarted(url: String, canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update {
            it.copy(
                currentUrl = url,
                isLoading = true,
                loadProgress = 12,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                errorState = null
            )
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { state ->
            val shouldDismissSplash = state.showInitialSplash && progress >= 75
            state.copy(
                loadProgress = progress,
                isLoading = progress < 100,
                showInitialSplash = if (shouldDismissSplash) false else state.showInitialSplash
            )
        }
    }

    fun onPageFinished(
        url: String,
        title: String?,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        _uiState.update {
            it.copy(
                currentUrl = url,
                pageTitle = title?.takeIf { t -> t.isNotBlank() && t != "about:blank" } ?: "Phoenix",
                loadProgress = 100,
                isLoading = false,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                showInitialSplash = false
            )
        }
    }

    fun onTitleReceived(title: String?) {
        if (!title.isNullOrBlank() && title != "about:blank") {
            _uiState.update { it.copy(pageTitle = title) }
        }
    }

    fun onWebError(failedUrl: String, errorCode: Int, description: String) {
        val online = networkMonitor.isCurrentlyOnline()
        _uiState.update {
            it.copy(
                isLoading = false,
                loadProgress = 0,
                showInitialSplash = false,
                isOnline = online,
                errorState = WebErrorState(
                    failedUrl = failedUrl,
                    errorCode = errorCode,
                    description = description,
                    isOffline = !online
                )
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorState = null, isLoading = true, loadProgress = 10) }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showInitialSplash = false) }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDesktopMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDesktopMode(enabled)
        }
    }

    fun setImmersiveMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setImmersiveMode(enabled)
        }
    }

    fun setOpenExternalLinksOutside(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOpenExternalLinksOutside(enabled)
        }
    }

    class Factory(
        private val preferencesRepository: PhoenixPreferencesRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhoenixViewModel(
                preferencesRepository = preferencesRepository,
                networkMonitor = networkMonitor
            ) as T
        }
    }
}
