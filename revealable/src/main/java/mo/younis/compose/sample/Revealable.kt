@file:OptIn(ExperimentalFoundationApi::class)

package mo.younis.compose.sample

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
class RevealableState(
    private val allowMultipleReveals: Boolean,
    private val coroutineScope: CoroutineScope,
) {
    private var revealedItem by weakReference<RevealableItemState?>()

    internal fun onItemRevealed(itemState: RevealableItemState) {
        if (allowMultipleReveals) return
        if (revealedItem == itemState) return
        revealedItem?.let { coroutineScope.launch { it.animateTo(RevealableValue.Initial) } }
        revealedItem = itemState
    }
}

typealias RevealableItemState = AnchoredDraggableState<RevealableValue>

@Stable
private enum class RevealableDirection {
    Start,
    End,
}

@Stable
enum class RevealableValue {
    Initial,
    StartRevealed,
    EndRevealed,
}

@Composable
fun rememberRevealableItemState(
    initialValue: RevealableValue = RevealableValue.Initial,
    positionalThreshold: (totalDistance: Float) -> Float,
    velocityThreshold: () -> Float,
    animationSpec: AnimationSpec<Float> = spring(),
    confirmValueChange: (newValue: RevealableValue) -> Boolean = { true },
): RevealableItemState = rememberSaveable(
    saver = RevealableItemState.Saver(
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
    ),
) {
    RevealableItemState(
        initialValue = initialValue,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
    )
}

@Composable
fun rememberRevealableState(
    allowMultipleReveals: Boolean = false,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) = remember {
    RevealableState(allowMultipleReveals = allowMultipleReveals, coroutineScope = coroutineScope)
}

@Composable
fun Revealable(
    state: RevealableState,
    modifier: Modifier,
    enable: Boolean = true,
    positionalThreshold: (totalDistance: Float) -> Float = { distance -> distance * 0.5f },
    velocityThreshold: (Density) -> Float = { density -> with(density) { 150.dp.toPx() } },
    confirmValueChange: (newValue: RevealableValue) -> Boolean = { true },
    startContent: @Composable () -> Unit = {},
    endContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var startContentSize by remember { mutableStateOf(IntSize.Zero) }
    var endContentSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val itemState = rememberRevealableItemState(
        positionalThreshold = positionalThreshold,
        velocityThreshold = { velocityThreshold(density) },
        confirmValueChange = confirmValueChange,
    )

    val dragStartProgress by remember {
        itemState.progressFor(RevealableValue.StartRevealed, layoutDirection.revealDirection)
    }

    val dragEndProgress by remember {
        itemState.progressFor(RevealableValue.EndRevealed, layoutDirection.revealDirection)
    }

    LaunchedEffect(itemState) {
        launch {
            snapshotFlow { startContentSize }
                .collectLatest {
                    itemState.updateAnchors(
                        newAnchors = makeAnchors(
                            endContentSize = endContentSize,
                            startContentSize = startContentSize,
                            layoutDirection = layoutDirection,
                        ),
                        newTarget = itemState.targetValue,
                    )
                }
        }

        launch {
            snapshotFlow { endContentSize }
                .collectLatest {
                    itemState.updateAnchors(
                        newAnchors = makeAnchors(
                            endContentSize = endContentSize,
                            startContentSize = startContentSize,
                            layoutDirection = layoutDirection,
                        ),
                        newTarget = itemState.targetValue,
                    )
                }
        }

        launch {
            snapshotFlow { itemState.targetValue }
                .collectLatest { value ->
                    if (value != RevealableValue.Initial) {
                        state.onItemRevealed(itemState)
                    }
                }
        }
    }

    val startProgressProvider = remember {
        {
            dragStartProgress
        }
    }

    val endProgressProvider = remember {
        {
            dragEndProgress
        }
    }

    Box(
        modifier = Modifier.then(
            if (enable) {
                Modifier.revealableItem(itemState)
            } else {
                Modifier
            },
        ),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .onSizeChanged { startContentSize = it },
            ) {
                RevealableRow(
                    progressProvider = startProgressProvider,
                    direction = RevealableDirection.Start,
                ) {
                    startContent()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .onSizeChanged { endContentSize = it },
            ) {
                RevealableRow(
                    progressProvider = endProgressProvider,
                    direction = RevealableDirection.End,
                ) {
                    endContent()
                }
            }
        }

        Box(
            modifier = modifier.offset {
                val x = itemState.offset.toInt()
                    .times(if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
                IntOffset(x = x, y = 0)
            },
        ) {
            content()
        }
    }
}

private fun makeAnchors(
    startContentSize: IntSize,
    endContentSize: IntSize,
    layoutDirection: LayoutDirection,
) = DraggableAnchors {
    RevealableValue.EndRevealed at endContentSize.width.toFloat()
        .times(if (layoutDirection == LayoutDirection.Ltr) -1 else 1)
    RevealableValue.Initial at 0f
    RevealableValue.StartRevealed at startContentSize.width.toFloat()
        .times(if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
}

@Composable
fun Modifier.revealableItem(
    state: RevealableItemState,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = this then Modifier.anchoredDraggable(
    state = state,
    orientation = Orientation.Horizontal,
    interactionSource = interactionSource,
)

private fun RevealableItemState.progressFor(
    value: RevealableValue,
    direction: RevealableDirection,
) = derivedStateOf(structuralEqualityPolicy()) {
    val mOffset = if (direction == RevealableDirection.Start) offset else -offset

    if (value == RevealableValue.StartRevealed && mOffset < 0f) {
        return@derivedStateOf 0f
    }

    if (value == RevealableValue.EndRevealed && mOffset > 0f) {
        return@derivedStateOf 0f
    }

    val position = anchors.positionOf(value)

    if (position.isNaN().not() && mOffset.isNaN().not()) {
        var progress = mOffset.absoluteValue / position.absoluteValue
        progress = if (progress.isNaN()) {
            0f
        } else {
            progress.coerceIn(0f, 1f)
        }
        Log.d("RevealSwipe", "progressFor: $value: $progress")
        progress
    } else {
        0f
    }
}

@Composable
private fun RevealableRow(
    modifier: Modifier = Modifier,
    direction: RevealableDirection,
    progressProvider: () -> Float,
    content: @Composable () -> Unit,
) {
    val measurePolicy = revealableRowMeasurePolicy(progressProvider, direction)
    Layout(
        measurePolicy = measurePolicy,
        content = content,
        modifier = modifier,
    )
}

private fun revealableRowMeasurePolicy(
    progressProvider: () -> Float,
    direction: RevealableDirection,
) = MeasurePolicy { measurables, constraints ->
    val placeables = measurables.map { measurable -> measurable.measure(constraints) }
    val height = placeables.maxOf { it.height }
    val width = placeables.sumOf { it.width }

    layout(width, height) {
        placeables.forEachIndexed { index, placeable ->
            val zIndex =
                if (direction == RevealableDirection.End) index.toFloat() else (placeables.size - index).toFloat()

            val placeableWidth = placeable.width

            val previousPlaceableWidth = placeables.subList(0, index).sumOf { it.width }

            val nextPlaceableWidth = if (index != placeables.lastIndex) {
                placeables
                    .subList(min(placeables.lastIndex, index + 1), placeables.size)
                    .sumOf { it.width }
            } else {
                0
            }

            val progress = progressProvider()

            val xPos = when (direction) {
                RevealableDirection.Start -> {
                    (-placeableWidth + (progress * placeableWidth) + (previousPlaceableWidth * progress)).roundToInt()
                }

                RevealableDirection.End -> {
                    (width - ((progress * placeableWidth) + (nextPlaceableWidth * progress))).roundToInt()
                }
            }

            placeable.placeRelative(
                x = xPos,
                y = 0,
                zIndex = zIndex,
            )
        }
    }
}

private val LayoutDirection.revealDirection: RevealableDirection
    get() = when (this) {
        LayoutDirection.Ltr -> RevealableDirection.Start
        LayoutDirection.Rtl -> RevealableDirection.End
    }
