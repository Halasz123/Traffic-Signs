package com.example.trafficsigns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.trafficsigns.databinding.ActivityMainBinding
import com.example.trafficsigns.ui.fragments.Home.MainScreenFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //supportFragmentManager.beginTransaction().replace(R.id.main_framelayout, MainScreenFragment()).commit()
    }

}
