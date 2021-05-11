package com.trafficsigns.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.trafficsigns.R
import com.trafficsigns.databinding.ActivityMainBinding

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Initializing the main layout.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

}
