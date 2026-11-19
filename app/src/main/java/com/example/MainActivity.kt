package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PhoenixPreferencesRepository
import com.example.ui.PhoenixViewModel
import com.example.ui.components.ImmersiveExitPill
import com.example.ui.components.PhoenixBottomBar
import com.example.ui.components.PhoenixErrorOverlay
import com.example.ui.components.PhoenixNavigationRail
import com.example.ui.components.PhoenixSettingsSheet
import com.example.ui.components.PhoenixSplashOverlay
import com.example.ui.components.PhoenixTopBar
import com.example.ui.components.PhoenixWebController
import com.example.ui.components.PhoenixWebView
import com.example.ui.components.launchExternalUri
import com.example.ui.theme.PhoenixTheme
import com.example.util.NetworkMonitor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContext = applicationContext
        val viewModelFactory = PhoenixViewModel.Factory(
            preferencesRepository = PhoenixPreferencesRepository(appContext),
            networkMonitor = NetworkMonitor(appContext)
        )

        setContent {
            val viewModel: PhoenixViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            PhoenixTheme(themeMode = uiState.settings.themeMode) {
                PhoenixApp(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun PhoenixApp(
    viewModel: PhoenixViewModel,
    uiState: com.example.ui.PhoenixUiState
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val webController = remember { PhoenixWebController() }

    val showMessage: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val shareCurrentPage: () -> Unit = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, uiState.pageTitle)
            putExtra(Intent.EXTRA_TEXT, "${uiState.pageTitle}\n${uiState.currentUrl}")
        }
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.nav_share))
        )
    }

    val copyCurrentLink: () -> Unit = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Phoenix URL", uiState.currentUrl))
        showMessage(context.getString(R.string.msg_link_copied))
    }

    val openInBrowser: () -> Unit = {
        val opened = launchExternalUri(context, uiState.currentUrl)
        if (!opened) {
            showMessage(context.getString(R.string.msg_external_app_unavailable))
        }
    }

    BackHandler(enabled = uiState.isSettingsOpen || uiState.canGoBack || uiState.settings.immersiveMode) {
        when {
            uiState.isSettingsOpen -> viewModel.setSettingsOpen(false)
            uiState.canGoBack -> webController.goBack()
            uiState.settings.immersiveMode -> viewModel.setImmersiveMode(false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavigationRail && !uiState.settings.immersiveMode) {
                        PhoenixBottomBar(
                            canGoBack = uiState.canGoBack,
                            canGoForward = uiState.canGoForward,
                            onBackClick = { webController.goBack() },
                            onForwardClick = { webController.goForward() },
                            onHomeClick = {
                                viewModel.clearError()
                                webController.loadHome()
                            },
                            onShareClick = shareCurrentPage,
                            onSettingsClick = { viewModel.setSettingsOpen(true) }
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail && !uiState.settings.immersiveMode) {
                        PhoenixNavigationRail(
                            canGoBack = uiState.canGoBack,
                            canGoForward = uiState.canGoForward,
                            onBackClick = { webController.goBack() },
                            onForwardClick = { webController.goForward() },
                            onHomeClick = {
                                viewModel.clearError()
                                webController.loadHome()
                            },
                            onRefreshClick = {
                                viewModel.clearError()
                                webController.reload()
                            },
                            onShareClick = shareCurrentPage,
                            onSettingsClick = { viewModel.setSettingsOpen(true) }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .imePadding()
                    ) {
                        AnimatedVisibility(
                            visible = !uiState.settings.immersiveMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            PhoenixTopBar(
                                pageTitle = uiState.pageTitle,
                                currentUrl = uiState.currentUrl,
                                isOnline = uiState.isOnline,
                                isLoading = uiState.isLoading,
                                loadProgress = uiState.loadProgress,
                                onHomeClick = {
                                    viewModel.clearError()
                                    webController.loadHome()
                                },
                                onRefreshClick = {
                                    viewModel.clearError()
                                    webController.reload()
                                },
                                onShareClick = shareCurrentPage,
                                onCopyLinkClick = copyCurrentLink,
                                onOpenInBrowserClick = openInBrowser,
                                onToggleImmersiveClick = { viewModel.setImmersiveMode(true) },
                                onOpenSettingsClick = { viewModel.setSettingsOpen(true) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            PhoenixWebView(
                                controller = webController,
                                desktopMode = uiState.settings.desktopMode,
                                openExternalLinksOutside = uiState.settings.openExternalLinksOutside,
                                isLoading = uiState.isLoading,
                                onPageStarted = viewModel::onPageStarted,
                                onProgressChanged = viewModel::onProgressChanged,
                                onPageFinished = viewModel::onPageFinished,
                                onTitleReceived = viewModel::onTitleReceived,
                                onWebError = viewModel::onWebError,
                                onShowMessage = showMessage,
                                modifier = Modifier.fillMaxSize()
                            )

                            val errorState = uiState.errorState
                            if (errorState != null) {
                                PhoenixErrorOverlay(
                                    errorState = errorState,
                                    onRetryClick = {
                                        viewModel.clearError()
                                        webController.loadUrl(errorState.failedUrl)
                                    },
                                    onGoHomeClick = {
                                        viewModel.clearError()
                                        webController.loadHome()
                                    },
                                    onOpenBrowserClick = openInBrowser
                                )
                            }

                            ImmersiveExitPill(
                                visible = uiState.settings.immersiveMode,
                                onExitImmersive = { viewModel.setImmersiveMode(false) },
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            }
        }

        PhoenixSplashOverlay(
            visible = uiState.showInitialSplash,
            onDismissClick = { viewModel.dismissSplash() }
        )

        if (uiState.isSettingsOpen) {
            PhoenixSettingsSheet(
                settings = uiState.settings,
                currentUrl = uiState.currentUrl,
                onDismiss = { viewModel.setSettingsOpen(false) },
                onThemeModeChange = viewModel::setThemeMode,
                onDesktopModeChange = viewModel::setDesktopMode,
                onImmersiveModeChange = viewModel::setImmersiveMode,
                onExternalLinksChange = viewModel::setOpenExternalLinksOutside,
                onClearCacheClick = {
                    webController.clearCache()
                    showMessage(context.getString(R.string.msg_cache_cleared))
                },
                onOpenInBrowserClick = openInBrowser
            )
        }
    }
}
