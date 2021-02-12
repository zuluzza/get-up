package com.zuluzza.getup

import android.util.Log

class StepConditionChecker {
    private var mLastStepCount = 0
    private var mLastStepCountSet = false
    private var intervalStepsGoal = 250

    enum class status {
        SUCCEEDED,
        INSUFFICIENT
    }

    fun check(newStepCount: Int): status {
        Log.d(TAG, "newStepCount($newStepCount), old($mLastStepCount)")
        if (mLastStepCount > (newStepCount - intervalStepsGoal) && mLastStepCountSet) {
            //not that many steps taken, create a notification to user
            Log.d(TAG, "Step count is insufficient")
            mLastStepCount = newStepCount
            mLastStepCountSet = true
            return status.INSUFFICIENT
        }
        mLastStepCount = newStepCount
        mLastStepCountSet = true
        Log.d(TAG, "Enough steps in the last interval")
        return status.SUCCEEDED
    }

    fun setInitial(stepCount: Int) {
        if (!mLastStepCountSet) {
            Log.d(TAG, "Setting initial step count $stepCount")
            mLastStepCount = stepCount
            mLastStepCountSet = true
        }
    }

    fun setGoal(steps: Int) {
        if (steps > 0) {
            Log.d(TAG, "new step goal $steps")
            intervalStepsGoal = steps
        }

    }

    fun getStepGoal(): Int { return intervalStepsGoal}
}