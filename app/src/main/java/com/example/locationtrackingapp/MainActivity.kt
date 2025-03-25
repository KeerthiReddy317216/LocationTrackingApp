package com.example.locationtrackingapp

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.locationtrackingapp.databinding.ActivityMainBinding
import com.example.locationtrackingapp.utils.Utils
import com.example.locationtrackingapp.view.LocationFragment


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var fragment:LocationFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        fragment = LocationFragment(applicationContext)
        val transition = supportFragmentManager.beginTransaction()
        transition.add(R.id.frame_layout,fragment,"Location").commit()

    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.stopLocationTracking(this)
    }
}