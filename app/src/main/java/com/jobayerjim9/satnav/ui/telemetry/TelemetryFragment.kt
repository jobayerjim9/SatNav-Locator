package com.jobayerjim9.satnav.ui.telemetry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.razorpay.*
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.FragmentTelemetryBinding
import com.jobayerjim9.satnav.ui.models.Constants
import com.jobayerjim9.satnav.ui.models.TelemetryData
import com.jobayerjim9.satnav.ui.models.UserProfile
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class TelemetryFragment : Fragment() {
    lateinit var binding: FragmentTelemetryBinding
    lateinit var listener: ValueEventListener
    lateinit var telemetryAdapter: TelemetryAdapter
    private var telemetries: ArrayList<TelemetryData> = ArrayList()
    private var telemetriesBackup: ArrayList<TelemetryData> = ArrayList()
    lateinit var database: DatabaseReference
    lateinit var userRef: DatabaseReference
    lateinit var listenerUser: ValueEventListener
    private var prices: ArrayList<Double> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_telemetry, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        prices.add(49.00)
        prices.add(399.00)
        binding.loading = true
        userRef = Firebase.database.reference.child(Constants.USER_PATH)
            .child(Firebase.auth.uid.toString())

        val calendar = Calendar.getInstance()
//        calendar.timeInMillis=1623261887*1000L
//        val date: String = DateFormat.format("dd-MM-yyyy", calendar).toString()
//        Log.d("millisConvert",date)
        listenerUser = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(UserProfile::class.java)
                binding.loading = false
                if (userProfile != null) {
                    binding.userData = userProfile
                    if (userProfile.subscribed) {
                        loadTelemetry()
                        if (userProfile.expireSubscription.isNotEmpty()) {
                            checkSubscription()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loading = false
            }

        }



        userRef.addValueEventListener(listenerUser)
        binding.subscribe.setOnClickListener {
            if (binding.monthlyRadio.isChecked) {
                loadPaymentGateway(0)
            } else if (binding.yearlyRadio.isChecked) {
                loadPaymentGateway(1)
            } else {
                Toast.makeText(requireContext(), "Select A Subscription Type!", Toast.LENGTH_LONG)
                    .show()
            }


        }
//        Thread {
//            try {
//

//                Log.d("orderDetails", order.toJson().toString())
//            } catch (e: RazorpayException) {
//                // Handle Exception
//                println(e.message)
//            }
//        }.start()

//        Thread {
//            val jsonObject: JSONObject= JSONObject();
//
//            val listPlans = razorpayClient.Customers.fetch()
//
//            Log.d("subscriptions",listPlans[0].toJson().toString())
//            //Log.d("plansRazorPay",listPlans[0].toJson().getJSONObject("customer_details").getString("email"))
//
//
//        }.start()


    }

    private fun checkSubscription() {
        val today = Calendar.getInstance()
        today.add(Calendar.MONTH, 1)
        val subsDate = Calendar.getInstance()
        val dates = binding.userData!!.expireSubscription.split("-")
        subsDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[0]))
        subsDate.set(Calendar.MONTH, Integer.parseInt(dates[1]))
        subsDate.set(Calendar.YEAR, Integer.parseInt(dates[2]))
        if (today.after(subsDate)) {
            userRef.child("subscribed").setValue(false)
        }
        Log.d("subsDate",
            subsDate.get(Calendar.DAY_OF_MONTH)
                .toString() + " " + subsDate.get(Calendar.MONTH) + " " + subsDate.get(Calendar.YEAR)
        )
        Log.d("today",
            today.get(Calendar.DAY_OF_MONTH)
                .toString() + " " + today.get(Calendar.MONTH) + " " + today.get(Calendar.YEAR)
        )

    }

    private fun loadPaymentGateway(type: Int) {
        Constants.SUBS_TYPE = type
        val amount = prices[type]
        val razorpayClient =
            RazorpayClient(Constants.RAZORPAY_KEY_ID, Constants.RAZORPAY_KEY_SECRET)
        Thread {
            binding.loading = true
            val checkout = Checkout()
            checkout.setKeyID(Constants.RAZORPAY_KEY_ID)
            val orderRequest = JSONObject()
            orderRequest.put("amount", amount * 100) // amount in the smallest currency unit
            orderRequest.put("currency", "INR")
            val order: Order = razorpayClient.Orders.create(orderRequest)
            val options = JSONObject()
            options.put("name", binding.userData!!.name)
            //options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("order_id", order.toJson().get("id")) //from response of step 3.

            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", amount) //pass amount in currency subunits

            options.put("prefill.email", binding.userData!!.email)
            options.put("prefill.contact", binding.userData!!.phoneNumber)
            val retryObj = JSONObject()
            retryObj.put("enabled", false)
            retryObj.put("max_count", 1)
            options.put("retry", retryObj)
            checkout.open(requireActivity(), options)
            binding.loading = false
        }.start()
    }

    fun loadTelemetry() {
        binding.telemetryRecycler.layoutManager = LinearLayoutManager(requireContext())
        telemetryAdapter = TelemetryAdapter(requireContext(), telemetries)
        binding.telemetryRecycler.adapter = telemetryAdapter
        binding.loading = true
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                telemetries.clear()
                for (dataSnapshot in snapshot.children) {
                    val data = dataSnapshot.getValue(TelemetryData::class.java)
                    telemetries.add(data!!)
                    telemetryAdapter.notifyDataSetChanged()
                }
                telemetriesBackup.addAll(telemetries)
                binding.loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        }
        binding.searchTerm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val term = s.toString()
                telemetries.clear()
                if (term.isEmpty()) {
                    telemetries.addAll(telemetriesBackup)
                    telemetryAdapter.notifyDataSetChanged()
                } else {
                    for (value in telemetriesBackup) {

                        if (value.satelliteName.toLowerCase().contains(term.toLowerCase())) {
                            telemetries.add(value)
                        }
                        telemetryAdapter.notifyDataSetChanged()
                    }

                }
            }
        })
        database = Firebase.database.reference.child(Constants.TELEMETRY_PATH)
        database.addValueEventListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::database.isInitialized) {
            database.removeEventListener(listener)
        }
        if (this::userRef.isInitialized) {
            userRef.removeEventListener(listenerUser)
        }

    }

    override fun onPause() {
        super.onPause()
        if (this::userRef.isInitialized) {
            userRef.removeEventListener(listenerUser)
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::userRef.isInitialized) {
            userRef.addValueEventListener(listenerUser)
        }
    }

}