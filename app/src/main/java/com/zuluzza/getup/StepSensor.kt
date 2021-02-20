package com.zuluzza.getup

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.concurrent.TimeUnit

class StepSensor: JobService(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mStepSensor: Sensor? = null
    private var receivedInitialValue = false

    companion object {
        private var isListening = false
        private var mStepCount = 0

        fun getStepCount(): Int {
            return mStepCount
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "StepSensor onStopJob listening=$isListening")
        if (isListening) {
            mSensorManager?.unregisterListener(this, mStepSensor)
        }
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "StepSensor onStartCommand isListening=$isListening")
        if (!isListening) {
            if (mSensorManager == null) {
                mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            if (mStepSensor == null) {
                mStepSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            }

            val result = mSensorManager?.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL, TimeUnit.SECONDS.toMicros(10).toInt())
            isListening = true
            Log.d(TAG, "StepSensor registered $result")
        }
        return false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "StepSensor got now event ${event.toString()}")
        if (event == null) return
        mStepCount = event.values[0].toInt()
        Log.d(TAG, "newStepCount=$mStepCount (initial=$receivedInitialValue")
        if (!receivedInitialValue) {
            MainActivity.stepConditionChecker.setInitial(mStepCount)
            receivedInitialValue = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do
    }
}