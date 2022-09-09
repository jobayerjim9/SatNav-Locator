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
package com.jobayerjim9.satnav.ui.prefsScreen

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.utility.PrefsManager
import com.jobayerjim9.satnav.utility.QthConverter
import com.jobayerjim9.satnav.utility.round
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrefsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var qthConverter: QthConverter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setPositionFromGPS()
            } else {
                showSnack(getString(R.string.pref_pos_gps_error))
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(PrefsManager.keyPositionGPS)?.apply {
            setOnPreferenceClickListener {
                setPositionFromGPS()
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>(PrefsManager.keyPositionQTH)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                setPositionFromQth(newValue.toString())
            }
        }

        findPreference<EditTextPreference>(PrefsManager.keyRotatorAddress)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (Patterns.IP_ADDRESS.matcher(newValue.toString()).matches()) {
                    return@setOnPreferenceChangeListener true
                } else {
                    showSnack(getString(R.string.tracking_rotator_address_invalid))
                    return@setOnPreferenceChangeListener false
                }
            }
        }

        findPreference<EditTextPreference>(PrefsManager.keyRotatorPort)?.apply {
            setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
            setOnPreferenceChangeListener { _, newValue ->
                val portValue = newValue.toString()
                if (portValue.isNotEmpty() && portValue.toInt() in 1024..65535) {
                    return@setOnPreferenceChangeListener true
                } else {
                    showSnack(getString(R.string.tracking_rotator_port_invalid))
                    return@setOnPreferenceChangeListener false
                }
            }
        }
    }

    private fun setPositionFromQth(qthString: String): Boolean {
        qthConverter.qthToLocation(qthString)?.let { gsp ->
            prefsManager.setStationPosition(gsp.latitude, gsp.longitude, gsp.heightAMSL)
            showSnack(getString(R.string.pref_pos_success))
            return true
        }
        showSnack(getString(R.string.pref_pos_qth_error))
        return false
    }

    private fun setPositionFromGPS() {
        val locPermString = Manifest.permission.ACCESS_FINE_LOCATION
        val locPermResult = ContextCompat.checkSelfPermission(requireContext(), locPermString)
        if (locPermResult == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (location != null) {
                val latitude = location.latitude.round(4)
                val longitude = location.longitude.round(4)
                val altitude = location.altitude.round(1)
                prefsManager.setStationPosition(latitude, longitude, altitude)
                showSnack(getString(R.string.pref_pos_success))
            } else showSnack(getString(R.string.pref_pos_gps_null))
        } else requestPermissionLauncher.launch(locPermString)
    }

    private fun showSnack(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.nav_bottom)
            .show()
    }
}
