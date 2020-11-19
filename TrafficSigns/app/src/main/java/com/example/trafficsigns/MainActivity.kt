package com.example.trafficsigns

import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class MainActivity : AppCompatActivity() {
    private var myData = ""
    private lateinit var trafficSigns: MutableMap<String, TrafficSign>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadData()
        Log.d("Main", myData)
        findViewById<View>(R.id.button).apply {
            this.setOnClickListener {
               // Log.d("Main", myData)
                trafficSigns.forEach {
                    Log.d("Main", "${it.key } -- ${it.value}")
                }
            }
        }
    }

    private fun downloadData() {
        lifecycleScope.launch {
            myData= withContext(Dispatchers.IO) { downloadDataBlocking() }
            val gson = Gson()
            val mapType = object : TypeToken<MutableMap<String, TrafficSign>>() {}.type
            val list: MutableMap<String, TrafficSign> = gson.fromJson(myData, mapType)
            //Log.d("Main", tutorials.toString())
            trafficSigns = list
        }
    }


    private fun downloadDataBlocking(): String {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.dropbox.com/s/6osm7j4tyee0kqf/traffic_signs.json?dl=1").build()
        val response = client.newCall(request).execute()
        return response.body()?.string() ?: ""
    }
}