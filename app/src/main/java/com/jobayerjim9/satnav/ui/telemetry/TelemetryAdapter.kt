package com.jobayerjim9.satnav.ui.telemetry

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ItemTelemetryBinding
import com.jobayerjim9.satnav.ui.models.TelemetryData

class TelemetryAdapter(
    private val context: Context,
    private val telemetries: ArrayList<TelemetryData>
) : RecyclerView.Adapter<TelemetryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemTelemetryBinding? = DataBindingUtil.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_telemetry, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = telemetries[position]
        holder.binding?.data = data
        holder.binding?.item?.setOnClickListener {
            holder.binding.loading = true
            val intent = Intent(context, TelemetryActivity::class.java)
            intent.putExtra("data", data)
            context.startActivity(intent)
            object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    holder.binding.loading = false
                }

            }.start()


        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return telemetries.size
    }
}