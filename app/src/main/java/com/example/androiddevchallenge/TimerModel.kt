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

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TimerModel : ViewModel() {

    private var job: Job? = null
    private var totalTime = 0

    val remainingTime = MutableStateFlow(totalTime)
    val progress = MutableStateFlow(0.0f)
    val running = MutableStateFlow(false)
    val formattedRemainingTime = MutableStateFlow("00:00")

    override fun onCleared() {
        job?.cancel()
    }

    fun start() {
        stop()
        val numbers = (0 until remainingTime.value).reversed().toList()
        running.value = true

        job = GlobalScope.launch {
            numbers.asFlow().onEach {
                delay(1000)
            }.onEach(::onTick).collect()
        }
    }

    fun stop() {
        job?.cancel()
        running.value = false
    }

    fun clear() {
        stop()
        setTotalTime(0)
    }

    fun toggle() {
        if (isRunning()) {
            stop()
        } else {
            start()
        }
    }

    fun updateTotalTime(percent: Float) {
        setTotalTime((percent * 60.0f).toInt())
    }

    private fun isRunning() = job?.isActive == true

    private fun remainingPercent() =
        if (totalTime > 0) (remainingTime.value.toFloat() / totalTime.toFloat()) else 0.0f

    private fun updateRemainingTime(seconds: Int) {
        remainingTime.value = seconds

        val m = (seconds / 60)
        val s = seconds % 60
        formattedRemainingTime.value =
            "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"

        progress.value = remainingPercent()
    }

    private fun onTick(seconds: Int) {
        updateRemainingTime(seconds)
        running.value = seconds > 0
    }

    private fun setTotalTime(seconds: Int) {
        totalTime = seconds
        updateRemainingTime(seconds)
    }
}
