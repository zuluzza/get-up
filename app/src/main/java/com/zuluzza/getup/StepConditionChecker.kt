package com.zuluzza.getup

import android.util.Log

class StepConditionChecker {
    private var mLastStepCount = 0
    private var mLastTimestampMs: Long = 0
    val STEPS_TO_TAKE_IN_INTERVAL = 250

    enum class status {
        SUCCEEDED,
        INSUFFICIENT
    }

    fun check(newStepCount: Int): status {
        mLastTimestampMs = System.currentTimeMillis()
        if (mLastStepCount > (newStepCount) - STEPS_TO_TAKE_IN_INTERVAL) {
            //not that many steps taken, create a notification to user
            Log.d(TAG, "Step count is insufficient")
            return status.INSUFFICIENT
        }
        mLastStepCount = newStepCount
        Log.d(TAG, "Enough steps in the last interval")
        return status.SUCCEEDED
    }

    fun getLastCheckTimestamp(): Long { return mLastTimestampMs }
}