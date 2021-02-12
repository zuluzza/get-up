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

        val newStepCount = StepSensor.getStepCount()
        val nextAction = MainActivity.stepConditionChecker.check(newStepCount)

        if (nextAction == StepConditionChecker.status.INSUFFICIENT) {
            Log.d(TAG, "AlarmReceiver sending insufficient step count notification")
            MainActivity.sendNotification()
        }
        MainActivity.setAlarm(CHECK_INTERVAL_MIN)
    }
}