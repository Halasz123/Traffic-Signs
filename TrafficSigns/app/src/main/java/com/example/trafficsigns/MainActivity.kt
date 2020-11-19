package com.example.trafficsigns

import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {
    private var myData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadData()
    }

    private fun downloadData() {
        lifecycleScope.launch {
            myData= withContext(Dispatchers.IO) { downloadDataBlocking() }
            Log.d("Main", myData)

        }
    }


    private fun downloadDataBlocking(): String {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.dropbox.com/s/sw9jv0khlpu68uc/traffic_signs.json?dl=1").build()
        val response = client.newCall(request).execute()
        return response.body()?.string() ?: ""
    }
}