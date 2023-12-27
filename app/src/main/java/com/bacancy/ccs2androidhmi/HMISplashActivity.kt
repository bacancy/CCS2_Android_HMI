package com.bacancy.ccs2androidhmi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bacancy.ccs2androidhmi.views.Dashboard
import com.bacancy.ccs2androidhmi.views.NewTestActivity

class HMISplashActivity : AppCompatActivity() {

    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hmisplash)
        handler.postDelayed(runnable, 3000)
    }

    private val runnable = Runnable {
        startActivity(Intent(this@HMISplashActivity, NewTestActivity::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}