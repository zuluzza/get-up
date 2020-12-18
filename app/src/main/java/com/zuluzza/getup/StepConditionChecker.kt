package com.zuluzza.getup

class StepConditionChecker {
    private var mLastStepCount = 0
    private var mLastTimestampMs: Long = 0
    val STEPS_TO_TAKE_IN_INTERVAL = 250
    var CHECK_INTERVAL_MS = 60 * 60 * 1000

    enum class status {
        SUCCEEDED,
        INSUFFICIENT
    }

    fun check(newStepCount: Int): status {
        mLastTimestampMs = System.currentTimeMillis()
        if (mLastStepCount > (newStepCount) - STEPS_TO_TAKE_IN_INTERVAL) {
            //not that many steps taken, create a notification to user
            return status.INSUFFICIENT
        }
        mLastStepCount = newStepCount
        return status.SUCCEEDED
    }

    fun getLastCheckTimestamp(): Long { return mLastTimestampMs }
}