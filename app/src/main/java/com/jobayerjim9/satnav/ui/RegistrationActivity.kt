package com.jobayerjim9.satnav.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityRegistrationBinding
import com.jobayerjim9.satnav.ui.models.EmailRegModel
import com.jobayerjim9.satnav.ui.models.RegModel

class RegistrationActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)
        initView()

    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun initView() {
        binding.data = RegModel()
        binding.signUpButton.setOnClickListener {
            if (binding!!.data?.email!!.isEmpty()) {
                Toast.makeText(this@RegistrationActivity, "Enter Your Email", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding!!.data?.password!!.isEmpty()) {
                Toast.makeText(this@RegistrationActivity, "Enter A Password", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding!!.data?.confirmPassword!!.isEmpty()) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Confirm your password",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding!!.data?.password != binding!!.data?.confirmPassword) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Password Doesn't Match!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signUp()
            }
        }

    }

    private fun signUp() {
        binding.loading = true
        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(binding!!.data!!.email, binding!!.data!!.password)
            .addOnCompleteListener(this@RegistrationActivity) {
                if (it.isSuccessful) {
                    uploadData()

                } else {
                    binding.loading = false
                    Toast.makeText(
                        this@RegistrationActivity,
                        it.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    private fun uploadData() {
        val auth = Firebase.auth
        val database = Firebase.database.reference
        val data = EmailRegModel(binding.data!!.email, auth!!.uid.toString(), "email")

        database.child("users").child(auth!!.uid.toString()).setValue(data)
            .addOnCompleteListener(this@RegistrationActivity) {
                binding.loading = false
                if (it.isSuccessful) {
                    startActivity(Intent(this@RegistrationActivity, Look4SatActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@RegistrationActivity,
                        it.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }
}