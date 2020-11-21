package com.example.trafficsigns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class MainActivity : AppCompatActivity() {
    private var myData = ""
    private var trafficSigns: HashMap<String, ArrayList<TrafficSign>> = HashMap()
    private lateinit var mTrafficSignsCollectionViewModel: TrafficSignsCollectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTrafficSignsCollectionViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        downloadData()


        findViewById<View>(R.id.button).apply {
            this.setOnClickListener {
                trafficSigns.forEach {
                    Log.d("Main", "${it.key } -- ${it.value}")
                    mTrafficSignsCollectionViewModel.addTrafficSignsCollection(
                        TrafficSignsCollection(it.key, it.value)
                    )
                }
            }
        }
    }

    private fun downloadData() {
        lifecycleScope.launch {
            myData= withContext(Dispatchers.IO) { downloadDataBlocking() }
            val gson = Gson()
            val type = object : TypeToken<MutableMap<String, TrafficSign>>() {}.type
            val list: MutableMap<String, TrafficSign> = gson.fromJson(myData, type)
            list.forEach {
                if (trafficSigns[it.value.group] != null) {
                    trafficSigns[it.value.group]!!.add(it.value)
                }
                else {
                    it.value.group?.let { it1 -> trafficSigns.put(it1, arrayListOf(it.value)) }
                }
            }
        }
    }


    private fun downloadDataBlocking(): String {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.dropbox.com/s/6osm7j4tyee0kqf/traffic_signs.json?dl=1").build()
        val response = client.newCall(request).execute()
        return response.body()?.string() ?: ""
    }
}
