package com.expofp.minimap.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expofp.minimap.ui.components.PlanMapView

private const val MINI_MAP_HEIGHT_DP = 280

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitorDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExhibitorDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val presenter by viewModel.presenter.collectAsStateWithLifecycle()
    val isExpanded = uiState.isMapExpanded

    DisposableEffect(Unit) {
        viewModel.onScreenEnter()
        onDispose { viewModel.onScreenExit() }
    }

    BackHandler(enabled = isExpanded) {
        viewModel.toggleMapExpanded()
    }

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) { 0.dp } else { 16.dp },
        label = "cornerRadius"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (isExpanded) { 0.dp } else { 16.dp },
        label = "horizontalPadding"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(visible = !isExpanded) {
            TopAppBar(
                title = { Text(uiState.exhibitorName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Track the content collapse animation using MutableTransitionState.
        // When the animation finishes (isIdle && hidden), we apply pending directions —
        // this ensures selectRoute() runs after the map has reached its full-screen size.
        val contentVisibleState = remember { MutableTransitionState(!isExpanded) }
        contentVisibleState.targetState = !isExpanded

        LaunchedEffect(contentVisibleState.isIdle, contentVisibleState.currentState) {
            if (contentVisibleState.isIdle && !contentVisibleState.currentState) {
                viewModel.consumePendingDirections()
            }
        }

        AnimatedVisibility(visibleState = contentVisibleState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = uiState.exhibitorName,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = LOREM_IPSUM,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // The map stays in the same position in the composable tree at all times —
        // no detach/reattach of the WebView. When expanded, it fills remaining space
        // via weight(1f); when collapsed, it has a fixed height.
        Box(
            modifier = (if (isExpanded) { Modifier.weight(1f) } else { Modifier.height(MINI_MAP_HEIGHT_DP.dp) })
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            presenter?.let { p ->
                PlanMapView(
                    presenter = p,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Block touch interactions on the mini map so it acts as a static preview.
            if (!isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                }
                            }
                        }
                )
            }

            if (presenter != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .then(if (isExpanded) { Modifier.systemBarsPadding() } else { Modifier })
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (isExpanded) {
                                viewModel.toggleDirections()
                            } else {
                                viewModel.expandWithDirections()
                            }
                        },
                        containerColor = if (isExpanded && uiState.isDirectionsActive) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = viewModel::toggleMapExpanded,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.CloseFullscreen
                            } else {
                                Icons.Default.OpenInFull
                            },
                            contentDescription = null
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = !isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Placeholder text for demonstration purposes.
private const val LOREM_IPSUM =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris."
