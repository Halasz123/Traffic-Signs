package com.example.trafficsigns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.data.TrafficSignsCollectionViewModel
import com.example.trafficsigns.databinding.ActivityMainBinding
import com.example.trafficsigns.ui.fragments.MainScreenFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.main_framelayout, MainScreenFragment()).commit()
    }

}
