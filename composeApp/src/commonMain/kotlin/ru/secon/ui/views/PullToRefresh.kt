package com.niksah.gagarin.utils.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


/** Pull to refresh используемые в дизайн системе. */
object PullToRefreshes {

    /**
     * Основной Pull to refresh.
     *
     * @param modifier [Modifier] применяемый к компоненту.
     * @param state Состояние компонента, для создания использовать [rememberPullToRefreshState].
     * @param onRefresh Коллбек, выхываемый когдап пользователь вызывает обновление контента.
     * @param enabled `true` если с компонентом можно взаимодействовать.
     * @param content Контент располагаемый внутри компонента.
     */
    @Composable
    fun Primary(
        modifier: Modifier = Modifier,
        state: PullToRefreshState,
        onRefresh: () -> Unit,
        enabled: Boolean = true,
        content: @Composable () -> Unit
    ) {
        // Минимальное расстояние свайпаа, которое вызвает обновление
        val refreshTriggerDistance = 88.dp
        val clipIndicatorToPadding = true
        val refreshingOffset = 72.dp
        val dragMultiplier = 0.5f

        require(refreshingOffset <= refreshTriggerDistance) { "refreshingOffset must be <= refreshTriggerDistance" }

        val coroutineScope = rememberCoroutineScope()
        val updatedOnRefresh by rememberUpdatedState(onRefresh)
        val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }

        LaunchedEffect(Unit) {
            snapshotFlow { !state.isPullInProgress to state.isRefreshing }
                .distinctUntilChanged()
                .filter { it.first }
                .map { it.second }
                .collectLatest { isRefreshing -> state.animateOffsetTo(if (isRefreshing) refreshingOffsetPx else 0f) }
        }

        val nestedScrollConnection = remember(state, coroutineScope) {
            NestedScrollConnectionImpl(state, coroutineScope) { updatedOnRefresh() }
        }.apply {
            this.enabled = enabled
            this.dragMultiplier = dragMultiplier
            this.refreshTrigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
        }

        Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
            Box(modifier = Modifier.offset {
                IntOffset(
                    0,
                    state.contentOffset.toInt()
                )
            }) { content() }
            Box(
                modifier = Modifier
                    .let { if (!clipIndicatorToPadding) it.clipToBounds() else it }
                    .padding(PaddingValues(0.dp))
                    .matchParentSize()
                    .let { if (clipIndicatorToPadding) it.clipToBounds() else it }
            ) {
                Box(Modifier.align(Alignment.TopCenter)) {
                    CircularIndicator(
                        state = state,
                        refreshTriggerDistance = refreshTriggerDistance,
                        refreshingOffset = refreshingOffset
                    )
                }
            }
        }
    }
}

@Composable
fun Circular(
    modifier: Modifier = Modifier,
    percentage: Float
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .size(size = 24.dp)
            .progressSemantics()
            .padding(all = 2.dp)
    ) {
        drawArc(
            brush = Brush.sweepGradient(
                0f to color.copy(alpha = 0f),
                0.9f to color,
                1f to color
            ),
            startAngle = 5f,
            sweepAngle = percentage * 360f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Создает стейт [PullToRefreshState].
 *
 * @param isRefreshing Изменения в [isRefreshing] приведут к обновлению [PullToRefreshState].
 */
@Composable
fun rememberPullToRefreshState(isRefreshing: Boolean): PullToRefreshState =
    remember { PullToRefreshState(isRefreshing) }
        .apply { this.isRefreshing = isRefreshing }

/**
 * Состояние [SmartwayPullToRefreshes].
 *
 * @property
 */
@Stable
class PullToRefreshState internal constructor(isRefreshing: Boolean) {
    private val _contentOffset = Animatable(initialValue = 0f)

    var isRefreshing by mutableStateOf(isRefreshing)
    val isResting: Boolean get() = _contentOffset.isRunning && !isRefreshing
    val contentOffset: Float get() = _contentOffset.value
    var isPullInProgress: Boolean by mutableStateOf(value = false)
        internal set

    internal suspend fun animateOffsetTo(offset: Float) {
        _contentOffset.animateTo(offset)
    }

    internal suspend fun dispatchScrollDelta(delta: Float) {
        _contentOffset.snapTo(_contentOffset.value + delta)
    }
}

/** Круговой индикатор. */
@Composable
private fun CircularIndicator(
    modifier: Modifier = Modifier,
    state: PullToRefreshState,
    refreshTriggerDistance: Dp,
    refreshingOffset: Dp
) {
    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }
    val indicatorHeight = 48.dp
    val indicatorHeightPx = with(LocalDensity.current) { indicatorHeight.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(indicatorHeight)
            .graphicsLayer {
                translationY = state.contentOffset - (refreshingOffsetPx + indicatorHeightPx) / 2
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isRefreshing) {
            Circular()
        } else {
            Circular(
                percentage = ((state.contentOffset - refreshTriggerPx / 2) / refreshTriggerPx * 2).coerceIn(
                    0f,
                    1f
                )
            )
        }
    }
}

@Composable
fun Circular(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateValue(
        initialValue = 0f,
        targetValue = 1f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1332,
                easing = LinearEasing
            )
        )
    )
    Circular(
        modifier = modifier.rotate(degrees = progress * 360f),
        percentage = 0.95f
    )
}


private class NestedScrollConnectionImpl(
    private val state: PullToRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {

    var enabled: Boolean = false
    var refreshTrigger: Float = 0f
    var dragMultiplier: Float = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = when {
        !enabled -> Offset.Zero
        state.isRefreshing -> Offset(0f, available.y)
        source == NestedScrollSource.Drag && available.y < 0 -> dragUp(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        state.isRefreshing -> Offset.Zero
        source == NestedScrollSource.Drag && available.y > 0 -> dragDown(available)
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!state.isRefreshing && state.contentOffset >= refreshTrigger) onRefresh()
        state.isPullInProgress = false
        return when {
            state.contentOffset != 0f -> available
            else -> Velocity.Zero
        }
    }

    private fun dragUp(available: Offset): Offset {
        state.isPullInProgress = true

        val newOffset = (available.y * dragMultiplier + state.contentOffset).coerceAtLeast(0f)
        val dragConsumed = newOffset - state.contentOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch { state.dispatchScrollDelta(dragConsumed) }
            Offset(x = 0f, y = available.y)
        } else {
            Offset.Zero
        }
    }

    private fun dragDown(available: Offset): Offset {
        state.isPullInProgress = true
        coroutineScope.launch { state.dispatchScrollDelta(available.y * dragMultiplier) }
        return Offset(x = 0f, y = available.y)
    }
}
