package com.jobayerjim9.satnav.ui.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ItemUsersBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.UserProfile
import java.util.*
import kotlin.collections.ArrayList

class UsersAdapter(private val context: Context, private val users: ArrayList<UserProfile>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemUsersBinding? = DataBindingUtil.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_users, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = users[position]
        holder.binding?.data = data
        if (data.photo.isNotEmpty()) {
            Picasso.get().load(data.photo).into(holder.binding?.profileImage2)
        } else {
            holder.binding?.profileImage2?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_baseline_account_circle_24
                )
            )
        }

        holder.binding?.approveButton?.setOnClickListener {
            val ref = Firebase.database.reference.child(Constants.USER_PATH).child(data.uid)
            holder.binding.loading = true
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, 1)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
                .toString() + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR)

            ref.child("expireSubscription").setValue(date).addOnSuccessListener {
                ref.child("subsType").setValue("By Admin")
                ref.child("subscribed").setValue(true).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Approved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, it.exception?.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    holder.binding.loading = false
                }
            }

        }
        holder.binding?.refuseButton?.setOnClickListener {
            val ref = Firebase.database.reference.child(Constants.USER_PATH).child(data.uid)
                .child("subscribed")
            holder.binding.loading = true
            ref.setValue(false).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Refused", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, it.exception?.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
                holder.binding.loading = false
            }
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return users.size
    }
}