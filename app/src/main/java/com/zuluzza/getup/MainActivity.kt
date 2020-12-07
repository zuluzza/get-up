package com.zuluzza.getup

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log

class MainActivity : WearableActivity(), SensorEventListener {
    val TAG = "GetUpApp"
    var mSensorManager: SensorManager? = null
    var mLastStepCount = 0
    var mLastTimestampMs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        var stepSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e(TAG, "Failed to retrieve step counter sensor")
        } else {
            mSensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val newStepCount = if (event != null) event.values[0] else 0
        if (mLastStepCount < (newStepCount) - 250) {
            mLastTimestampMs = System.currentTimeMillis()
        }
    }
}