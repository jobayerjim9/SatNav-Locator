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
package com.jobayerjim9.satnav.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.razorpay.PaymentResultListener
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityMainBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.UserProfile
import com.jobayerjim9.satnav.utility.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class Look4SatActivity : AppCompatActivity(), PaymentResultListener {

    @Inject
    lateinit var prefsManager: PrefsManager
    lateinit var databaseRef: DatabaseReference
    lateinit var listener: ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        Log.d("firebaseUid", Firebase.auth.uid.toString())
        if (Firebase.auth.currentUser != null) {
            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserProfile::class.java)
                    if (user != null) {
                        if (user.name.isEmpty() || user.institute.isEmpty() || user.email.isEmpty()) {
                            Toast.makeText(
                                this@Look4SatActivity,
                                "Please Fill Up All Data",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(
                                Intent(
                                    this@Look4SatActivity,
                                    ProfileActivity::class.java
                                )
                            )
                        } else if (user.photo.isEmpty()) {
                            Toast.makeText(
                                this@Look4SatActivity,
                                "Please Upload A Profile Picture",
                                Toast.LENGTH_LONG
                            ).show();
                            startActivity(
                                Intent(
                                    this@Look4SatActivity,
                                    ProfileActivity::class.java
                                )
                            )

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
            databaseRef = Firebase.database.reference.child(Constants.USER_PATH)
                .child(Firebase.auth.uid.toString())

            val binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            binding.navBottom.setupWithNavController(navHost.navController)
            if (prefsManager.isFirstLaunch()) {
                prefsManager.setFirstLaunchDone()
                navHost.navController.navigate(R.id.nav_dialog_splash)
            }

        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        if (this::databaseRef.isInitialized) {
            databaseRef.addValueEventListener(listener)
        }
    }

    override fun onPause() {
        super.onPause()
        databaseRef.removeEventListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::databaseRef.isInitialized) {
            databaseRef.removeEventListener(listener)
        }
    }


    override fun onPaymentSuccess(p0: String?) {
        val database = Firebase.database.reference.child(Constants.USER_PATH)
            .child(Firebase.auth.uid.toString())
        val calendar = Calendar.getInstance()
        var subsType: String = ""
        if (Constants.SUBS_TYPE == 0) {
            calendar.add(Calendar.MONTH, 1)
            subsType = "Monthly"
        } else if (Constants.SUBS_TYPE == 1) {
            calendar.add(Calendar.YEAR, 1)
            subsType = "Yearly"
        }
        val date = calendar.get(Calendar.DAY_OF_MONTH)
            .toString() + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR)

        database.child("expireSubscription").setValue(date).addOnSuccessListener {
            database.child("subscribed").setValue(true)
            database.child("subsType").setValue(subsType)
        }
        Log.d("paymentSuccessCalled", date)
        Toast.makeText(this@Look4SatActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.d("paymentError", p1!!.toString())
        Toast.makeText(this@Look4SatActivity, "Payment Cancelled Or Failed", Toast.LENGTH_SHORT)
            .show()

    }

}
