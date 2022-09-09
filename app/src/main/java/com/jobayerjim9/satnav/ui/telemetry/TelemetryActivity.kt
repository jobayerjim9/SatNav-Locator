package com.jobayerjim9.satnav.ui.telemetry

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.ActivityTelemetryBinding
import com.jobayerjim9.satnav.ui.models.TelemetryData

class TelemetryActivity : AppCompatActivity() {
    lateinit var binding: ActivityTelemetryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_telemetry)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.clearHistory()
        binding.mapView.destroy()
    }

    private fun initView() {
        val data = intent.getSerializableExtra("data") as TelemetryData
        binding.data = data
        binding.mapView.settings.javaScriptEnabled = true
        binding.mapView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.mapView.settings.builtInZoomControls = true
        binding.mapView.settings.domStorageEnabled = true
        binding.mapView.settings.displayZoomControls = true
        binding.mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.mapView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val action = event?.action
                return when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Disallow ScrollView to intercept touch events.
                        binding.scroll.requestDisallowInterceptTouchEvent(false)
                        // Disable touch on transparent view
                        false
                    }
                    MotionEvent.ACTION_UP -> {
                        // Allow ScrollView to intercept touch events.
                        binding.scroll.requestDisallowInterceptTouchEvent(false)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        binding.scroll.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    else -> true
                }
            }

        })
        binding.mapView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!);
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.mapLoading = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.mapLoading = false
            }
        }
        binding.mapView.loadUrl(data.satelliteMap)
        if (data.image.isNotEmpty()) {
            Picasso.get().load(data.image).into(binding.sateliteImage)
        }
        val adapter = PastTelemetryAdapter(this, data.pastTelemetries)
        binding.pastTelemetryRecycler.layoutManager = LinearLayoutManager(this)
        binding.pastTelemetryRecycler.adapter = adapter
        binding.backButton.setOnClickListener {
            finish()
        }

    }
}