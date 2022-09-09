package com.jobayerjim9.satnav.ui.telemetry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ItemPastTelemetryBinding
import com.jobayerjim9.satnav.ui.models.TelemetryData

class PastTelemetryAdapter(
    private val context: Context,
    private val pastTelemetries: ArrayList<TelemetryData.PastTelemetry>
) :
    RecyclerView.Adapter<PastTelemetryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: ItemPastTelemetryBinding? = DataBindingUtil.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_past_telemetry, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = pastTelemetries[position]
        holder.binding?.data = data
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return pastTelemetries.size
    }
}