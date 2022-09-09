package com.jobayerjim9.satnav.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityAdminBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.UserProfile
import java.util.*
import kotlin.collections.ArrayList


class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    private var users: ArrayList<UserProfile> = ArrayList()
    private var usersBackup: ArrayList<UserProfile> = ArrayList()
    lateinit var adapter: UsersAdapter
    lateinit var listener: ValueEventListener
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin)
        initView()

    }

    override fun onDestroy() {
        super.onDestroy()
        database.removeEventListener(listener)
    }

    private fun initView() {
        binding.usersRecycler.layoutManager = LinearLayoutManager(this)
        adapter = UsersAdapter(this, users)
        binding.usersRecycler.adapter = adapter
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                usersBackup.clear()
                for (snapshot1 in snapshot.children) {
                    val data = snapshot1.getValue(UserProfile::class.java)
                    if (data != null && data.type == Constants.TYPE_USER) {
                        users.add(data)
                        adapter.notifyDataSetChanged()
                    }

                }
                usersBackup.addAll(users)
                binding.loading = false

            }

            override fun onCancelled(error: DatabaseError) {
                binding.loading = false
            }


        }

        database = Firebase.database.reference.child(Constants.USER_PATH)
        binding.loading = true
        database.addValueEventListener(listener)
        binding.searchTerm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val term = s.toString()
                if (term.isEmpty()) {
                    users.clear()
                    users.addAll(usersBackup)
                    adapter.notifyDataSetChanged()
                } else {
                    users.clear()
                    for (user in usersBackup) {
                        if (user.email.toLowerCase(Locale.ROOT)
                                .contains(term.toLowerCase(Locale.ROOT)) || user.phoneNumber.toLowerCase(
                                Locale.ROOT
                            ).contains(term.toLowerCase(Locale.ROOT)) || user.uid.toLowerCase(
                                Locale.ROOT
                            ).contains(term.toLowerCase(Locale.ROOT)) || user.name.toLowerCase(
                                Locale.ROOT
                            ).contains(term.toLowerCase(Locale.ROOT)) || user.institute.toLowerCase(
                                Locale.ROOT
                            ).contains(term.toLowerCase(Locale.ROOT))
                        ) {
                            users.add(user)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

            }

        })
    }


}