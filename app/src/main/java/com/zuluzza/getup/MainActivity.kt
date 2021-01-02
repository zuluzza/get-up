package com.zuluzza.getup

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import androidx.core.app.NotificationCompat

val TAG = "GetUpApp"
val ALARM_REQUEST_CODE = 255
var CHECK_INTERVAL_MS: Long = 60 * 60 * 1000

class MainActivity : WearableActivity() {
    private var mAlarmManager: AlarmManager? = null
    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : NotificationCompat.Builder
    private val channelId = "com.zuluzza.getup.notifications"
    lateinit var context: Context
    init {
        instance = this;
    }
    // This is to pass around the application's context for alarm receiver
    companion object {
        private var instance: MainActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = applicationContext()

        val sensorFilter = IntentFilter()
        sensorFilter.addAction("com.zuluzza.getup.StepSensor")
        val sensorReceiver = SensorReceiver()
        sensorReceiver.setContext(this)
        registerReceiver(sensorReceiver, sensorFilter)

        Log.d(TAG, "MainActivity registered receiver and going to read step sensor")
        readStepSensor()
    }

    override fun onResume() {
        super.onResume()
        //TODO is this necessary?
        readStepSensor()
    }

    fun setAlarm(timeInMillis: Long) {
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (mAlarmManager == null) {
            Log.e(TAG, "mAlarmManger was not set while trying to set an alarm!")
            return
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, 0)
        Log.d(TAG, "Setting new alarm to $timeInMillis ms")
        // TODO is RTC_WAKEUP the best option?
        mAlarmManager!!.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(
                channelId, "Step notifications",NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun sendNotification() {
        createNotificationChannel();
        Log.d(TAG, "Sending notification")
        builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Get Up!")
                .setContentText("You have not taken as many steps as you'd wanted to. Now it's time to get up!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    fun readStepSensor() {
        startService(Intent(this, StepSensor::class.java))
    }

    private class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
                context: Context,
                intent: Intent
        ) {
            Log.d(TAG, "AlarmReceiver got new event")
            (applicationContext() as MainActivity).readStepSensor()
        }
    }

    private class SensorReceiver : BroadcastReceiver() {
        private var mainActivity: MainActivity? = null

        fun setContext(main: MainActivity) {
            mainActivity = main
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "SensorReceiver received intent ${intent.toString()}")

            if (intent == null) {
                Log.e(TAG, "null intent")
                return
            }
            if (intent.getBooleanExtra("setAlarm", true)) {
                Log.d(TAG, "SensorReceiver setting alarm")
                mainActivity?.setAlarm(CHECK_INTERVAL_MS)
            }
            if (intent.getBooleanExtra("sendNotification",  false)) {
                Log.d(TAG, "SensorReceiver sending notification")
                mainActivity?.sendNotification()
            }
        }
    }
}
