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
package com.jobayerjim9.satnav.ui.polarScreen

import androidx.lifecycle.*
import com.jobayerjim9.satnav.data.model.SatPass
import com.jobayerjim9.satnav.data.model.SatTrans
import com.jobayerjim9.satnav.data.repository.PassesRepo
import com.jobayerjim9.satnav.data.repository.SatelliteRepo
import com.jobayerjim9.satnav.di.IoDispatcher
import com.jobayerjim9.satnav.utility.PrefsManager
import com.jobayerjim9.satnav.utility.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.net.Socket
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PolarViewModel @Inject constructor(
    private val orientationProvider: OrientationProvider,
    private val prefsManager: PrefsManager,
    private val passesRepo: PassesRepo,
    private val satelliteRepo: SatelliteRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel(), OrientationProvider.OrientationListener {

    private val magDeclination = prefsManager.getMagDeclination()
    private val _transmitters = MutableLiveData<List<SatTrans>>()
    private val _azimuth = MutableLiveData<Float>()
    val transmitters: LiveData<List<SatTrans>> = _transmitters
    val azimuth: LiveData<Float> = _azimuth

    fun getPass(catNum: Int, aosTime: Long) = liveData {
        passesRepo.passes.collect { passes ->
            val pass = passes.find { it.catNum == catNum && it.aosDate.time == aosTime }
            pass?.let { satPass ->
                processTransmitters(satPass)
                initRotatorControl(satPass)
                emit(satPass)
            }
        }
    }

    fun enableSensor() {
        if (prefsManager.shouldUseCompass()) orientationProvider.startListening(this)
    }

    fun disableSensor() {
        if (prefsManager.shouldUseCompass()) orientationProvider.stopListening()
    }

    override fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float) {
        _azimuth.value = azimuth + magDeclination
    }

    private fun initRotatorControl(satPass: SatPass) {
        viewModelScope.launch {
            val rotatorPrefs = prefsManager.getRotatorServer()
            if (rotatorPrefs != null) {
                runCatching {
                    withContext(ioDispatcher) {
                        val socket = Socket(rotatorPrefs.first, rotatorPrefs.second)
                        val writer = socket.getOutputStream().bufferedWriter()
                        while (isActive) {
                            val satPos = satPass.predictor.getSatPos(Date())
                            val azimuth = Math.toDegrees(satPos.azimuth).round(1)
                            val elevation = Math.toDegrees(satPos.elevation).round(1)
                            writer.write("\\set_pos $azimuth $elevation")
                            writer.newLine()
                            writer.flush()
                            delay(1000)
                        }
                        writer.close()
                    }
                }.onFailure { Timber.d(it) }
            }
        }
    }

    private fun processTransmitters(satPass: SatPass) {
        viewModelScope.launch {
            satelliteRepo.getSatTransmitters(satPass.catNum).collect { transList ->
                while (isActive) {
                    val timeNow = Date()
                    val copiedList = transList.map { it.copy() }
                    copiedList.forEach { transmitter ->
                        transmitter.downlink?.let {
                            transmitter.downlink = satPass.predictor.getDownlinkFreq(it, timeNow)
                        }
                        transmitter.uplink?.let {
                            transmitter.uplink = satPass.predictor.getUplinkFreq(it, timeNow)
                        }
                    }
                    _transmitters.postValue(copiedList.map { it.copy() })
                    delay(1000)
                }
            }
        }
    }
}