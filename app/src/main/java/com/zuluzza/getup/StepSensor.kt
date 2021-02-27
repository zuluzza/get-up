package com.zuluzza.getup

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class StepSensor: SensorEventListener {
    private var receivedInitialValue = false

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
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do
    }
}