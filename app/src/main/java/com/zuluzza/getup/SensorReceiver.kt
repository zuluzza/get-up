package com.zuluzza.getup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class SensorReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "SensorReceiver received intent ${intent.toString()}")

        if (intent == null) {
            Log.e(TAG, "null intent")
            return
        }
        if (intent.getBooleanExtra("setAlarm", true)) {
            Log.d(TAG, "SensorReceiver setting alarm")
            MainActivity.setAlarm(CHECK_INTERVAL_MS)
        }
        if (intent.getBooleanExtra("sendNotification",  false)) {
            Log.d(TAG, "SensorReceiver sending notification")
            MainActivity.sendNotification()
        }
    }
}