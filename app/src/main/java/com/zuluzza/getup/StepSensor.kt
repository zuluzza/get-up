package com.zuluzza.getup

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log

class StepSensor: Service(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mStepSensor: Sensor? = null
    private val stepConditionChecker = StepConditionChecker()
    private var isListening = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "StepSensor onStartCommand isListening=$isListening")
        if (isListening) {
            return super.onStartCommand(intent, flags, startId)
        }

        if (mStepSensor == null) {
            mStepSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        }

        mSensorManager?.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        isListening = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "StepSensor got now event ${event.toString()}")
        if (event == null) return

        mSensorManager?.unregisterListener(this, mStepSensor)
        isListening = false
        val newStepCount = event.values[0].toInt()
        val nextAction = stepConditionChecker.check(newStepCount)
        val sendNotification = nextAction == StepConditionChecker.status.INSUFFICIENT

        val setAlarmIntent = Intent(this, MainActivity::class.java).apply {
            action = "com.zuluzza.getup.StepSensor"
            putExtra("setAlarm", true)
            putExtra("sendNotification", sendNotification)
        }
        sendBroadcast(setAlarmIntent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}