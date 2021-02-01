package com.zuluzza.getup

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

val TAG = "GetUpApp"
val ALARM_REQUEST_CODE = 255
var CHECK_INTERVAL_MS: Long = TimeUnit.MINUTES.toMillis(5)

class MainActivity : WearableActivity() {
    private var mAlarmManager: AlarmManager? = null
    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : NotificationCompat.Builder
    private val channelId = "com.zuluzza.getup.notifications"
    private var sensorFilter: IntentFilter? = null
    private var sensorReceiver: SensorReceiver? = null
    lateinit var context: Context
    init {
        instance = this;
    }
    // This is to pass around the application's context for alarm receiver
    companion object {
        private var instance: MainActivity? = null
        val stepConditionChecker = StepConditionChecker()

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        fun setAlarm(checkIntervalMs: Long) {
            instance?.setAlarm(checkIntervalMs)
        }

        fun sendNotification() {
            instance?.sendNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //stops OS from killing the process
        sendOngoingNotification()

        setContentView(R.layout.activity_main)
        context = applicationContext()

        if(sensorFilter == null) {
            sensorFilter = IntentFilter("com.zuluzza.getup.StepSensor")
            sensorReceiver = SensorReceiver()
        }
        registerReceiver(sensorReceiver, sensorFilter)

        Log.d(TAG, "MainActivity registered receiver and going to read step sensor")
        startStepSensor()
        setAlarm(CHECK_INTERVAL_MS)
    }

    override fun onResume() {
        super.onResume()
        //TODO is this necessary?
        startStepSensor()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorReceiver)
    }

    fun setAlarm(timeInMillis: Long) {
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (mAlarmManager == null) {
            Log.e(TAG, "mAlarmManger was not set while trying to set an alarm!")
            return
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, 0)
        val alarmTime = System.currentTimeMillis() + timeInMillis
        Log.d(TAG, "Setting new alarm to $alarmTime ($timeInMillis)")
        // TODO is RTC_WAKEUP the best option?
        mAlarmManager!!.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }

    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(
                channelId, "Step notifications",NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(true)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun sendNotification() {
        createNotificationChannel();
        Log.d(TAG, "Sending insufficient steps notification")
        builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Get Up!")
                .setContentText("You have not taken as many steps as you'd wanted to. Now it's time to get up!")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(longArrayOf(100,200,100,200))
        notificationManager.notify(12345, builder.build())
    }

    fun sendOngoingNotification() {
        createNotificationChannel();
        Log.d(TAG, "Sending ongoing notification")
        builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Get Up!")
                .setContentText("Get up! is running and will wake you up if you are not moving as much as you wanted to!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
        notificationManager.notify(1, builder.build())
    }

    fun startStepSensor() {
        val stepJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(123, ComponentName(this, StepSensor::class.java))
        val job = jobInfo.setRequiresCharging(false).setMinimumLatency(1).setOverrideDeadline(60*1000).build()
        stepJobScheduler.schedule(job)
    }
}
