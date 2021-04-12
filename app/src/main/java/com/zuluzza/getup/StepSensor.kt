package com.zuluzza.getup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class StepSensor: Service(), SensorEventListener {
    lateinit var builder : NotificationCompat.Builder
    private val channelId = "com.zuluzza.getup.notifications"
    private var receivedInitialValue = false
    val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    companion object {
        private var mStepCount = 0

        fun getStepCount(): Int {
            return mStepCount
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "StepSensor got now event ${event.toString()}")
        if (event == null) return
        mStepCount = event.values[0].toInt()
        Log.d(TAG, "newStepCount=$mStepCount (initial=$receivedInitialValue")
        if (!receivedInitialValue) {
            MainActivity.stepConditionChecker.setInitial(mStepCount)
            receivedInitialValue = true
            val intent = Intent(this, StepSensor::class.java)
            intent.action = ACTION_START_FOREGROUND_SERVICE
            startService(intent)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "GetUp! notification channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }

    fun startStepSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Get Up!")
                .setContentText("Get up! is running and will wake you up if you are not moving as much as you wanted to!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
        startForeground(1, builder.build())

        startStepSensor()

        return START_REDELIVER_INTENT //if killed schedules a restart
    }
}