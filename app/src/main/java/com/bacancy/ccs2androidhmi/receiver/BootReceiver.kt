package com.bacancy.ccs2androidhmi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bacancy.ccs2androidhmi.views.HMISplashActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: CALLED")
        if (intent?.action != null && intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "onReceive: INSIDE ACTION BOOT COMPLETED")
            Handler(Looper.getMainLooper()).postDelayed({
                val launchIntent = Intent(context, HMISplashActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(launchIntent)
            }, 5000)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}