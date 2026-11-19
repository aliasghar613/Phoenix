package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.StatusConnectedGreen
import com.example.ui.theme.StatusOfflineRed
import com.example.util.UrlPolicy

@Composable
fun PhoenixTopBar(
    pageTitle: String,
    currentUrl: String,
    isOnline: Boolean,
    isLoading: Boolean,
    loadProgress: Int,
    onHomeClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onToggleImmersiveClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = (loadProgress.coerceIn(0, 100) / 100f),
        label = "page_progress"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onHomeClick)
                        .testTag("top_home_badge")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = stringResource(R.string.nav_home),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOnline) StatusConnectedGreen else StatusOfflineRed
                                )
                        )
                        Text(
                            text = UrlPolicy.formatDisplayLocation(currentUrl),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.nav_refresh),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_share)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onShareClick()
                            },
                            modifier = Modifier.testTag("menu_share")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_copy_link)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onCopyLinkClick()
                            },
                            modifier = Modifier.testTag("menu_copy_link")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_open_browser)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.OpenInBrowser, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenInBrowserClick()
                            },
                            modifier = Modifier.testTag("menu_open_browser")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_fullscreen)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Fullscreen, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleImmersiveClick()
                            },
                            modifier = Modifier.testTag("menu_immersive")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_settings)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Tune, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenSettingsClick()
                            },
                            modifier = Modifier.testTag("menu_settings")
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isLoading && loadProgress in 1..99,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun PhoenixBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                selected = false,
                onClick = onBackClick,
                enabled = canGoBack,
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back)
                    )
                },
                label = { Text(stringResource(R.string.nav_back)) },
                modifier = Modifier.testTag("bottom_back_button")
            )

            NavigationBarItem(
                selected = false,
                onClick = onForwardClick,
                enabled = canGoForward,
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = stringResource(R.string.nav_forward)
                    )
                },
                label = { Text(stringResource(R.string.nav_forward)) },
                modifier = Modifier.testTag("bottom_forward_button")
            )

            NavigationBarItem(
                selected = true,
                onClick = onHomeClick,
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = stringResource(R.string.nav_home)
                    )
                },
                label = { Text(stringResource(R.string.nav_home)) },
                modifier = Modifier.testTag("bottom_home_button")
            )

            NavigationBarItem(
                selected = false,
                onClick = onShareClick,
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = stringResource(R.string.nav_share)
                    )
                },
                label = { Text(stringResource(R.string.nav_share)) },
                modifier = Modifier.testTag("bottom_share_button")
            )

            NavigationBarItem(
                selected = false,
                onClick = onSettingsClick,
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.nav_settings)
                    )
                },
                label = { Text(stringResource(R.string.nav_settings)) },
                modifier = Modifier.testTag("bottom_settings_button")
            )
        }
    }
}

@Composable
fun PhoenixNavigationRail(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val railColors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        NavigationRailItem(
            selected = true,
            onClick = onHomeClick,
            colors = railColors,
            icon = {
                Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home))
            },
            label = { Text(stringResource(R.string.nav_home)) },
            modifier = Modifier.testTag("rail_home_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = onBackClick,
            enabled = canGoBack,
            colors = railColors,
            icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back)
                )
            },
            label = { Text(stringResource(R.string.nav_back)) },
            modifier = Modifier.testTag("rail_back_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = onForwardClick,
            enabled = canGoForward,
            colors = railColors,
            icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = stringResource(R.string.nav_forward)
                )
            },
            label = { Text(stringResource(R.string.nav_forward)) },
            modifier = Modifier.testTag("rail_forward_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = onRefreshClick,
            colors = railColors,
            icon = {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.nav_refresh)
                )
            },
            label = { Text(stringResource(R.string.nav_refresh)) },
            modifier = Modifier.testTag("rail_refresh_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = onShareClick,
            colors = railColors,
            icon = {
                Icon(
                    Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.nav_share)
                )
            },
            label = { Text(stringResource(R.string.nav_share)) },
            modifier = Modifier.testTag("rail_share_button")
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = onSettingsClick,
            colors = railColors,
            icon = {
                Icon(
                    Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) },
            modifier = Modifier
                .padding(bottom = 12.dp)
                .testTag("rail_settings_button")
        )
    }
}

@Composable
fun ImmersiveExitPill(
    visible: Boolean,
    onExitImmersive: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = onExitImmersive,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
                .minimumInteractiveComponentSize()
                .testTag("exit_immersive_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.FullscreenExit,
                contentDescription = stringResource(R.string.nav_exit_fullscreen)
            )
        }
    }
}
