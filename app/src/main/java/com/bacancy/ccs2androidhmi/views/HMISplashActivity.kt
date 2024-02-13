package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityHmisplashBinding


@SuppressLint("CustomSplashScreen")
class HMISplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHmisplashBinding
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHmisplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(runnable, 3000)
    }

    private val runnable = Runnable {
        startActivity(Intent(this@HMISplashActivity, HMIDashboardActivity::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

}