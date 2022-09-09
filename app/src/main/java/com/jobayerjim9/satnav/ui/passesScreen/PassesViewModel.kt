/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2021 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.jobayerjim9.satnav.ui.passesScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobayerjim9.satnav.data.model.Result
import com.jobayerjim9.satnav.data.model.SatPass
import com.jobayerjim9.satnav.data.repository.PassesRepo
import com.jobayerjim9.satnav.data.repository.SatelliteRepo
import com.jobayerjim9.satnav.utility.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class PassesViewModel @Inject constructor(
    private val prefsManager: PrefsManager,
    private val satelliteRepo: SatelliteRepo,
    private val passesRepo: PassesRepo
) : ViewModel() {

    private val _passes = MutableLiveData<Result<List<SatPass>>>(Result.InProgress)
    private var passesProcessing: Job? = null
    val passes: LiveData<Result<List<SatPass>>> = _passes

    init {
        viewModelScope.launch {
            _passes.postValue(Result.InProgress)
            passesRepo.triggerCalculation(satelliteRepo.getSelectedSatellites())
        }
        viewModelScope.launch {
            passesRepo.passes.collect { passes ->
                passesProcessing?.cancelAndJoin()
                passesProcessing = viewModelScope.launch { tickPasses(passes) }
            }
        }
    }

    fun forceCalculation() {
        viewModelScope.launch {
            _passes.postValue(Result.InProgress)
            passesProcessing?.cancelAndJoin()
            passesRepo.forceCalculation(satelliteRepo.getSelectedSatellites())
        }
    }

    fun shouldUseUTC(): Boolean {
        return prefsManager.shouldUseUTC()
    }

    private suspend fun tickPasses(passes: List<SatPass>) = withContext(Dispatchers.Default) {
        var currentPasses = passes
        while (isActive) {
            val timeNow = System.currentTimeMillis()
            currentPasses.forEach { pass ->
                if (!pass.isDeepSpace) {
                    val timeStart = pass.aosDate.time
                    if (timeNow > timeStart) {
                        val deltaNow = timeNow.minus(timeStart).toFloat()
                        val deltaTotal = pass.losDate.time.minus(timeStart).toFloat()
                        pass.progress = ((deltaNow / deltaTotal) * 100).toInt()
                    }
                }
            }
            currentPasses = currentPasses.filter { it.progress < 100 }
            _passes.postValue(Result.Success(currentPasses.map { it.copy() }))
            delay(1000)
        }
    }
}
