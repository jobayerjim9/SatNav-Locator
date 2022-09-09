package com.tsctech.satnav.utility

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.InputStream

class Utils {
    companion object {
        fun loadJSONFromAsset(context: Context, type: String): String? {
            Log.d("assetLoad","$type.json")
            return try {
                val ins: InputStream = context.assets.open("$type.json")
                val size = ins.available()
                val buffer = ByteArray(size)
                ins.read(buffer)
                ins.close()
                String(buffer, charset("UTF-8"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }
    }
}