package com.zuluzza.getup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log

class MainActivity : WearableActivity() {
    private val TAG = "GetUpApp"
    private var mAlarmManager: AlarmManager? = null
    private val stepSensor = StepSensor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stepSensor.startListeningStepEvents()
    }

    override fun onResume() {
        super.onResume()
        stepSensor.startListeningStepEvents()
    }

    private fun setAlarm(timeInMillis: Long) {
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (mAlarmManager == null) {
            Log.e(TAG, "mAlarmManger was not set while trying to set an alarm!")
            return
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        // TODO set requestcode and flags
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        // TODO is RTC_WAKEUP the best option?
        mAlarmManager!!.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    fun sendNotification() {
        //TODO
    }

    private class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
                context: Context,
                intent: Intent
        ) {

        }
    }
}