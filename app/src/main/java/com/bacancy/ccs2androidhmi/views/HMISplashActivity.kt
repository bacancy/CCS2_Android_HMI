package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bacancy.ccs2androidhmi.databinding.ActivityHmisplashBinding
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class HMISplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHmisplashBinding
    val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        binding = ActivityHmisplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(runnable, 3000)
    }

    private val runnable = Runnable {
        startActivity(Intent(this@HMISplashActivity, HMIDashboardActivity::class.java))
        finish()
    }

    private fun applyTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            // Device API level is 31 or higher
            if (prefHelper.getBoolean(IS_DARK_THEME, false)) uiManager.setApplicationNightMode(
                UiModeManager.MODE_NIGHT_YES
            ) else uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)

        } else {
            // Device API level is 30 or lower
            if (prefHelper.getBoolean(
                    IS_DARK_THEME,
                    false
                )
            ) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

}