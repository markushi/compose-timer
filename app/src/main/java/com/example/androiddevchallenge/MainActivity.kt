/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyApp() {
    val model = viewModel<TimerModel>()

    val remainingTime = model.remainingTime.collectAsState()
    val formattedRemainingTime = model.formattedRemainingTime.collectAsState()
    val isRunning = model.running.collectAsState()
    val progress = model.progress.collectAsState()

    val isDragging = remember { mutableStateOf(false) }
    val totalDragHeight = remember { mutableStateOf(0f) }

    val label = when {
        isDragging.value -> ""
        else ->
            when {
                remainingTime.value == 0 -> {
                    stringResource(R.string.label_timer_unset)
                }
                isRunning.value -> {
                    stringResource(R.string.label_timer_running)
                }
                else -> {
                    stringResource(R.string.label_timer_paused)
                }
            }
    }

    val valuePositionOffset = animateDpAsState(
        targetValue = if (isDragging.value) (-32).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    val backgroundColor =
        animateColorAsState(
            targetValue = INFERNO_LIGHT[(progress.value * (INFERNO_LIGHT.size - 1)).toInt()],
            animationSpec = tween(durationMillis = 1000)
        )

    Surface(color = backgroundColor.value) {
        val height = remember { mutableStateOf(0.0f) }
        val dragState = rememberDraggableState(
            onDelta = { delta ->
                totalDragHeight.value = (totalDragHeight.value - delta).coerceIn(0.0f, height.value)
                val p = (totalDragHeight.value / height.value).coerceIn(0.0f, 1.0f)
                model.updateTotalTime(p)
            }
        )
        Box(
            modifier = Modifier
                .onSizeChanged { size ->
                    height.value = size.height.toFloat()
                }
                .fillMaxSize()
                .combinedClickable(onClick = model::toggle, onLongClick = model::clear)
                .draggable(
                    dragState, orientation = Orientation.Vertical,
                    onDragStarted = {
                        isDragging.value = true
                        model.stop()
                    },
                    onDragStopped = {
                        isDragging.value = false
                        totalDragHeight.value = 0.0f
                        model.start()
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = valuePositionOffset.value),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val bounceOffset = infiniteTransition.animateFloat(
                    initialValue = 16.dp.value,
                    targetValue = 0.dp.value,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val bounceAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.0f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                val alpha = if (remainingTime.value != 0 || isDragging.value) 0.0f else bounceAlpha
                Icon(
                    modifier = Modifier
                        .width(32.dp)
                        .offset(y = bounceOffset.value.dp)
                        .alpha(alpha),
                    painter = painterResource(R.drawable.ic_baseline_keyboard_arrow_up_24),
                    contentDescription = null
                )
                Text(
                    fontSize = 64.sp,
                    text = formattedRemainingTime.value
                )
                Text(
                    modifier = Modifier.animateContentSize(),
                    fontSize = 16.sp,
                    text = label
                )
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
