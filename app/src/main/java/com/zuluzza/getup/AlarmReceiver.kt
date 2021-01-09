package com.zuluzza.getup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
            context: Context,
            intent: Intent
    ) {
        Log.d(TAG, "AlarmReceiver got new event")
        MainActivity.readStepSensor()
    }
}