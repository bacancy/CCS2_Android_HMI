package com.bacancy.ccs2androidhmi.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log


class MyDeviceAdminReceiver: DeviceAdminReceiver() {
    companion object {
        private const val TAG = "MyDeviceAdminReceiver"
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, MyDeviceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "onEnabled: Called")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        Log.i(TAG, "onDisableRequested: Called")
        return super.onDisableRequested(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "onDisabled: Called")
    }

    override fun onPasswordChanged(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordChanged(context, intent, user)
        Log.i(TAG, "onPasswordChanged: Called")
    }
}