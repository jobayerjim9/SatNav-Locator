package com.tsctech.satnav.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tsctech.satnav.R
import com.tsctech.satnav.databinding.ItemChooserBinding
import com.tsctech.satnav.utility.SelectItemListener

class ChooserAdapter(private val context: Context,private val names:ArrayList<String>,private val listener:SelectItemListener) :
    RecyclerView.Adapter<ChooserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemChooserBinding? = DataBindingUtil.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chooser,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.name=names[position]
        holder.binding?.item?.setOnClickListener {
            listener.selectedItem("",position)
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return names.size
    }
}