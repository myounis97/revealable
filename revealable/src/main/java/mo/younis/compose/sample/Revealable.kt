@file:OptIn(ExperimentalFoundationApi::class, ExperimentalCoroutinesApi::class)

package mo.younis.compose.sample

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

typealias RevealableState = AnchoredDraggableState<RevealableValue>

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
): RevealableState = rememberSaveable(
    saver = RevealableState.Saver(
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
    ),
) {
    RevealableState(
        initialValue = initialValue,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
    )
}

@Composable
fun Revealable(
    state: RevealableState,
    modifier: Modifier,
    enable: Boolean = true,
    startContent: @Composable () -> Unit = {},
    endContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var startContentSize by remember { mutableStateOf(IntSize.Zero) }
    var endContentSize by remember { mutableStateOf(IntSize.Zero) }

    val layoutDirection = LocalLayoutDirection.current

    val dragStartProgress by remember {
        state.progressFor(RevealableValue.StartRevealed, layoutDirection.revealDirection)
    }

    val dragEndProgress by remember {
        state.progressFor(RevealableValue.EndRevealed, layoutDirection.revealDirection)
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
                Modifier.revealableItem(state)
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
                    .onSizeChanged {
                        startContentSize = it
                        state.updateAnchors(
                            newAnchors = makeAnchors(
                                endContentSize = endContentSize,
                                startContentSize = startContentSize,
                                layoutDirection = layoutDirection,
                            ),
                            newTarget = state.targetValue,
                        )
                    },
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
                    .onSizeChanged {
                        endContentSize = it
                        state.updateAnchors(
                            newAnchors = makeAnchors(
                                endContentSize = endContentSize,
                                startContentSize = startContentSize,
                                layoutDirection = layoutDirection,
                            ),
                            newTarget = state.targetValue,
                        )
                    },
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
                val x = state.offset.toInt()
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
    state: RevealableState,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = this then Modifier.anchoredDraggable(
    state = state,
    orientation = Orientation.Horizontal,
    interactionSource = interactionSource,
)

private fun RevealableState.progressFor(
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
