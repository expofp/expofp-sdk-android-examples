package com.expofp.minimap.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expofp.minimap.ui.components.PlanMapView

private const val MINI_MAP_HEIGHT_DP = 200
private val CORNER_RADIUS = 18.dp
private val BLOCK_SHAPE = RoundedCornerShape(CORNER_RADIUS)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitorDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExhibitorDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExpanded = uiState.isMapExpanded

    DisposableEffect(Unit) {
        viewModel.onScreenEnter()
        onDispose { viewModel.onScreenExit() }
    }

    BackHandler(enabled = isExpanded) {
        viewModel.toggleMapExpanded()
    }

    // Track the overlay fade-out animation to run cleanup after it finishes
    val overlayVisibleState = remember { MutableTransitionState(false) }
    overlayVisibleState.targetState = isExpanded

    LaunchedEffect(overlayVisibleState.isIdle, overlayVisibleState.currentState) {
        if (overlayVisibleState.isIdle && !overlayVisibleState.currentState) {
            viewModel.onCollapseAnimationFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f))
    ) {
        // Layer 1: Normal detail content
        Column(modifier = Modifier.fillMaxSize()) {
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

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Text block before map
                Text(
                    text = LOREM_IPSUM,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BLOCK_SHAPE)
                        .background(Color.White)
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mini map
                Box(
                    modifier = Modifier
                        .height(MINI_MAP_HEIGHT_DP.dp)
                        .fillMaxWidth()
                        .clip(BLOCK_SHAPE)
                        .background(Color.White)
                ) {
                    viewModel.miniMapPresenter?.let { presenter ->
                        PlanMapView(
                            presenter = presenter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Block touch on mini map
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

                    if (viewModel.miniMapPresenter != null) {
                        PlanButtons(
                            isExpanded = false,
                            onExpandToggle = { viewModel.toggleMapExpanded() },
                            onDirections = { viewModel.expandWithDirections() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text block after map
                Text(
                    text = LOREM_IPSUM,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BLOCK_SHAPE)
                        .background(Color.White)
                        .padding(16.dp)
                )
            }
        }

        // Layer 2: Full-screen overlay
        AnimatedVisibility(
            visibleState = overlayVisibleState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                viewModel.fullMapPresenter?.let { presenter ->
                    PlanMapView(
                        presenter = presenter,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (viewModel.fullMapPresenter != null) {
                    PlanButtons(
                        isExpanded = true,
                        onExpandToggle = { viewModel.toggleMapExpanded() },
                        onDirections = { viewModel.showDirections() },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanButtons(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Expand/Collapse button — circle, white background, no elevation
        IconButton(
            onClick = onExpandToggle,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.CloseFullscreen
                } else {
                    Icons.Default.OpenInFull
                },
                contentDescription = null,
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Directions button — capsule with icon + text, no elevation
        Button(
            onClick = onDirections,
            shape = RoundedCornerShape(50),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Directions", color = Color.Black)
        }
    }
}

private const val LOREM_IPSUM =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
