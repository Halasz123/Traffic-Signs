package com.example.trafficsigns

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Property
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule
import android.animation.ObjectAnimator.ofFloat as ofFloat


class SplashScreen: AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding
    private var myData = ""
    private var trafficSigns: HashMap<String, ArrayList<TrafficSign>> = HashMap()
    private lateinit var mTrafficSignsCollectionViewModel: TrafficSignsCollectionViewModel
    private var downloaded = false
    private lateinit var internetErrorToast: Toast

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.splash_screen)
        mTrafficSignsCollectionViewModel = ViewModelProvider(this).get(
            TrafficSignsCollectionViewModel::class.java
        )
        internetErrorToast = Toast.makeText(this, "Please turn on the internet!", Toast.LENGTH_LONG)
        downloadData()
        //newAnim()
        animateLogo()

        val intent = Intent(this, MainActivity::class.java)
        Timer().schedule(4000){
            //while (!downloaded)

            startActivity(intent)
            overridePendingTransition(R.anim.fade_out, R.anim.splash_anim);
            finish()
        }

    }

    private fun newAnim() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val path = Path()
            path.arcTo(
                0f,
                0f,
                0f,
                0f,
                0f,
                359f,
                true
            ) //with first four parameters you determine four edge of a rectangle by pixel , and fifth parameter is the path'es start point from circle 360 degree and sixth parameter is end point of path in that circle
            val animator: ObjectAnimator = ofFloat(binding.stopImageview, "translationX", "translationY" , path)
            animator.duration = 3000
            animator.startDelay = 1000
            animator.start()
        }

    }

    private fun animateLogo() {
        val fromUp = ofFloat(binding.stopImageview, "translationY", -1500f, 50f).apply {
            duration = 2000
        }
        val toUp = ofFloat(binding.stopImageview, "translationY", 50f, -200f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
        }
        val fromLeft = ofFloat(binding.warningImageview, "translationX", -1500f, 50f).apply {
            duration = 2000
        }
        val toLeft = ofFloat(binding.warningImageview, "translationX", 50f, -200f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
        }
        val fromRight = ofFloat(binding.roundaboutImageview, "translationX", 1500f, -50f).apply {
            duration = 2000
        }
        val toRight = ofFloat(binding.roundaboutImageview, "translationX", -50f, 200f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
        }
        val rotateStop = ofFloat(binding.stopImageview, "rotation", 0f, 360f).apply {
            duration = 700
            repeatCount = Animation.INFINITE
        }
        val rotateWarning = rotateStop.clone()
        rotateWarning.target = binding.warningImageview

        val rotateRound = rotateStop.clone()
        rotateRound.target = binding.roundaboutImageview

        val asd = AnimatorSet().apply {
            play(fromUp).with(rotateStop)
            play(fromLeft).with(rotateWarning)
            play(fromRight).with(rotateRound)
            play(toUp).after(fromUp)
            play(toLeft).after(fromLeft)
            play(toRight).after(fromRight)
            start()
        }
    }

    private fun downloadData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                downloadDataBlocking()
            }
        }
    }


    private fun downloadDataBlocking(){
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.dropbox.com/s/6osm7j4tyee0kqf/traffic_signs.json?dl=1").build()
        return client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                myData = response.body()?.string() ?: ""
                parseJson()

            }
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Splashscreen", e.toString())
                internetErrorToast.show()
            }
        })
    }

    private fun parseJson() {
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
            Log.d("Splash", "${it.key} -- ${it.value}")
            mTrafficSignsCollectionViewModel.addTrafficSignsCollection(
                TrafficSignsCollection(it.key, it.value)
            )
        }

    }

}