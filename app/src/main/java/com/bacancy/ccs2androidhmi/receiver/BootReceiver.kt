package com.bacancy.ccs2androidhmi.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.HMISplashActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: CALLED")
        if (intent?.action != null && intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "onReceive: INSIDE ACTION BOOT COMPLETED")
            if(!isActivityRunning(context,HMISplashActivity::class.java) && !isActivityRunning(context,HMIDashboardActivity::class.java)){
                val launchIntent = Intent(context, HMISplashActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(launchIntent)
            }
        }
    }

    private fun isActivityRunning(context: Context?,activityClass: Class<*>): Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.appTasks.any { task ->
            task.taskInfo.baseActivity?.className == activityClass.name
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}