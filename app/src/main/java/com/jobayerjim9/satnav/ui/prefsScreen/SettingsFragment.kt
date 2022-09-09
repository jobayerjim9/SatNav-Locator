package com.jobayerjim9.satnav.ui.prefsScreen

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.FragmentSettingsBinding
import com.jobayerjim9.satnav.ui.LoginActivity
import com.jobayerjim9.satnav.ui.ProfileActivity
import com.jobayerjim9.satnav.ui.admin.AdminActivity
import com.jobayerjim9.satnav.ui.infoScreen.InfoActivity
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.UserProfile


class SettingsFragment : Fragment() {
    lateinit var binding: FragmentSettingsBinding
    lateinit var listener: ValueEventListener
    lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        initView()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        database.removeEventListener(listener)
    }

    private fun initView() {
        binding.settingsButton.setOnClickListener {
            val navHost =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            navHost.navController.navigate(R.id.prefsFragment)
        }
        binding.aboutUsButton.setOnClickListener {
            startActivity(Intent(requireActivity(), InfoActivity::class.java))
        }
        binding.editProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
        }
        binding.loading = true
        database = Firebase.database.reference.child(Constants.USER_PATH)
            .child(Firebase.auth.uid.toString())
        database.keepSynced(true)
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserProfile::class.java)
                if (user != null) {
                    Log.d("userData", user.photo)
                    if (user.photo.isNotEmpty()) {
                        Picasso.get().load(user.photo).into(binding.profileImage)
                    }
                    binding.name = user.name
                    binding.email = user.email
                    if (user.subscribed) {
                        binding.subscriptionDetail.text = user.subsType + " Subscription"
                    } else {
                        binding.subscriptionDetail.text = "No Active Subscription"
                    }
                    if (user.type == Constants.TYPE_ADMIN) {
                        binding.adminButton.visibility = View.VISIBLE
                        binding.adminButton.setOnClickListener {
                            startActivity(Intent(requireActivity(), AdminActivity::class.java))
                        }
                    } else {
                        binding.adminButton.visibility = View.GONE
                    }
                }
                binding.loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                binding.loading = false
            }

        }
        database.addValueEventListener(listener)
        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Are You Sure?")
                .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        database.removeEventListener(listener)
                        Firebase.auth.signOut()
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()


                    }

                })
                .setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }

                })
                .show()
        }
    }
}