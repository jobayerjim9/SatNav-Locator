package com.jobayerjim9.satnav.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityLoginBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.EmailRegModel
import com.jobayerjim9.satnav.ui.models.LoginModel
import com.jobayerjim9.satnav.ui.models.PhoneRegModel

class LoginActivity : AppCompatActivity() {
    final val PHONE_SIGN_IN = 2
    final val GOOGLE_SIGN_IN = 3
    final val FACEBOOK_SIGN_IN = 3
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        initView()
    }

    private fun initView() {
        binding.data = LoginModel()
        binding.loginButton.setOnClickListener {
            validateData()
        }
        binding.forgotPassword.setOnClickListener {
            if (binding.data!!.email.isBlank() || binding.data!!.email.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Enter Email", Toast.LENGTH_SHORT).show()
            } else {
                val auth = Firebase.auth
                auth.sendPasswordResetEmail(binding.data!!.email)
                    .addOnCompleteListener(this@LoginActivity) {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Check You Email For Password Reset",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        binding.phone.setOnClickListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.PhoneBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.AppTheme)
                    .setAvailableProviders(providers)
                    .build(),
                PHONE_SIGN_IN
            )
        }
        binding.google.setOnClickListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.AppTheme)
                    .setAvailableProviders(providers)
                    .build(),
                GOOGLE_SIGN_IN
            )
        }
        binding.facebook.setOnClickListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.FacebookBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.AppTheme)
                    .setAvailableProviders(providers)
                    .build(),
                FACEBOOK_SIGN_IN
            )
        }
        binding.createAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = IdpResponse.fromResultIntent(data)
        if (requestCode == PHONE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val user = Firebase.auth.currentUser
                Log.d("signIn", "successful")
                checkPhoneLogin(response)
            } else {
                if (response != null) {
                    Toast.makeText(
                        this@LoginActivity,
                        response!!.error!!.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (requestCode == GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = Firebase.auth.currentUser
                checkSocialLogin(response, Constants.TYPE_GOOGLE)
                Log.d("signIn", "successful" + user!!.email)
            } else {
                if (response != null) {
                    Toast.makeText(
                        this@LoginActivity,
                        response!!.error!!.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (requestCode == FACEBOOK_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = Firebase.auth.currentUser
                checkSocialLogin(response, Constants.TYPE_FACEBOOK)
                Log.d("signIn", "successful" + user!!.email)
            } else {
                if (response != null) {
                    Toast.makeText(
                        this@LoginActivity,
                        response!!.error!!.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun checkSocialLogin(response: IdpResponse?, type: String) {
        binding.loading = true
        val auth = Firebase.auth
        val database = Firebase.database.reference.child("users").child(auth!!.uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.loading = false
                    startActivity(Intent(this@LoginActivity, Look4SatActivity::class.java))
                    finish()
                } else {
                    val data =
                        EmailRegModel(response!!.email.toString(), auth!!.uid.toString(), type)
                    database.setValue(data).addOnCompleteListener(this@LoginActivity) {
                        binding.loading = false
                        if (it.isSuccessful) {
                            startActivity(Intent(this@LoginActivity, Look4SatActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                binding.loading = false
            }
        })

    }

    private fun checkPhoneLogin(response: IdpResponse?) {
        binding.loading = true
        val auth = Firebase.auth
        val database = Firebase.database.reference.child("users").child(auth!!.uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.loading = false
                    startActivity(Intent(this@LoginActivity, Look4SatActivity::class.java))
                    finish()
                } else {

                    val data =
                        PhoneRegModel(response!!.phoneNumber.toString(), auth!!.uid.toString())
                    database.setValue(data).addOnCompleteListener(this@LoginActivity) {
                        binding.loading = false
                        if (it.isSuccessful) {
                            startActivity(Intent(this@LoginActivity, Look4SatActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                binding.loading = false
            }
        })

    }

    private fun validateData() {
        if (binding.data!!.email.isBlank() || binding.data!!.email.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Enter Email", Toast.LENGTH_SHORT).show()
        } else if (binding.data!!.password.isBlank() || binding.data!!.password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Enter Password", Toast.LENGTH_SHORT).show()
        } else {
            binding.loading = true
            val firebaseAuth = Firebase.auth
            firebaseAuth.signInWithEmailAndPassword(binding.data!!.email, binding.data!!.password)
                .addOnCompleteListener(this@LoginActivity) {
                    Log.d("login", "complete")
                    binding.loading = false
                    if (it.isSuccessful) {
                        startActivity(Intent(this@LoginActivity, Look4SatActivity::class.java))
                        finish()
                    } else {
                        Log.d("loginResp", it.exception!!.message.toString() + " Failed")
                        Toast.makeText(
                            this@LoginActivity,
                            it.exception!!.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}