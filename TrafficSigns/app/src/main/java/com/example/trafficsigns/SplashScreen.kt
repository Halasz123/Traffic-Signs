package com.example.trafficsigns

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.SplashScreenBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import kotlin.concurrent.schedule


class SplashScreen: AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding
    private var myData = ""
    private var trafficSigns: HashMap<String, ArrayList<TrafficSign>> = HashMap()
    private lateinit var mTrafficSignsCollectionViewModel: TrafficSignsCollectionViewModel
    private var downloaded = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.splash_screen)
        mTrafficSignsCollectionViewModel = ViewModelProvider(this).get(TrafficSignsCollectionViewModel::class.java)
        downloadData()
        animateLogo()

        val intent = Intent(this, MainActivity::class.java)
        Timer().schedule(2700){
            //while (!downloaded)

            startActivity(intent)
            finish()
        }

    }

    private fun animateLogo() {
        binding.stopImageview.translationY = -1500f
        binding.stopImageview.animate()
            .translationY(0f)
            .setDuration(1000)
            .rotation(360f)
            .setInterpolator(LinearInterpolator())
            .setStartDelay(500)
            .start()

        binding.warningImageview.translationX = -1500f
        binding.warningImageview.animate()
            .translationX(0f)
            .setDuration(1000)
            .rotation(360f)
            .setInterpolator(LinearInterpolator())
            .setStartDelay(500)
            .start()

        binding.roundaboutImageview.translationX = 1500f
        binding.roundaboutImageview.animate()
            .translationX(0f)
            .setDuration(1000)
            .rotation(360f)
            .setInterpolator(LinearInterpolator())
            .setStartDelay(500)
            .start()

        binding.textView.alpha = 0f
        binding.textView.animate()
            .alpha(1f)
            .rotation(360f)
            .setDuration(1000)
            .setStartDelay(1500)
            .start()
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
            trafficSigns.forEach {
                Log.d("Splash", "${it.key } -- ${it.value}")
                mTrafficSignsCollectionViewModel.addTrafficSignsCollection(
                    TrafficSignsCollection(it.key, it.value)
                )
            }
            downloaded = true
        }
    }


    private fun downloadDataBlocking(): String {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.dropbox.com/s/6osm7j4tyee0kqf/traffic_signs.json?dl=1").build()
        val response = client.newCall(request).execute()
        return response.body()?.string() ?: ""
    }

}