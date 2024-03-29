package com.zuluzza.getup

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.time.ZoneOffset


val TAG = "GetUpApp"
val ALARM_REQUEST_CODE = 255
var CHECK_INTERVAL_MIN: Long = 60

class MainActivity : WearableActivity() {
    private var mAlarmManager: AlarmManager? = null
    lateinit var notificationManager : NotificationManager
    lateinit var builder : NotificationCompat.Builder
    private val channelId = "com.zuluzza.getup.notifications"
    private var sensorFilter: IntentFilter? = null
    private var sensorReceiver: SensorReceiver? = null
    private var startOfActivePeriod = 7
    private var endOfActivePeriod = 20

    lateinit var context: Context
    init {
        instance = this;
    }
    // This is to pass around the application's context for alarm receiver
    companion object {
        private var instance: MainActivity? = null
        val stepConditionChecker = StepConditionChecker()
        lateinit var notificationChannel : NotificationChannel

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        fun setAlarm(checkIntervalMs: Long) {
            instance?.setAlarm(checkIntervalMs)
        }

        fun sendNotification() {
            instance?.sendNotification()
        }

        fun getAppsNotificationChannel(): NotificationChannel {
            return notificationChannel
        }
    }

    private val startTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrEmpty()) {
                setActivePeriod(Integer.parseInt(s.toString()), endOfActivePeriod)
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
    private val endTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrEmpty()) {
                setActivePeriod(startOfActivePeriod, Integer.parseInt(s.toString()))
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private val stepGoalTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.isNullOrEmpty()) {
                stepConditionChecker.setGoal(Integer.parseInt(s.toString()))
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, StepSensor::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContentView(R.layout.activity_main)
        context = applicationContext()

        //set active period and step goal to layout
        val etStart = findViewById<View>(R.id.activePeriodStart) as EditText
        Log.d(TAG, "Initial active period is ${startOfActivePeriod.toString()}-${endOfActivePeriod.toString()}")
        etStart.setText(startOfActivePeriod.toString())
        etStart.addTextChangedListener(startTextWatcher)
        val etEnd = findViewById<View>(R.id.activePeriodEnd) as EditText
        etEnd.setText(endOfActivePeriod.toString())
        etEnd.addTextChangedListener(endTextWatcher)
        val etSteps = findViewById<View>(R.id.stepsGoal) as EditText
        etSteps.setText(stepConditionChecker.getStepGoal().toString())
        etSteps.addTextChangedListener(stepGoalTextWatcher)

        if(sensorFilter == null) {
            sensorFilter = IntentFilter("com.zuluzza.getup.StepSensor")
            sensorReceiver = SensorReceiver()
        }
        registerReceiver(sensorReceiver, sensorFilter)

        Log.d(TAG, "MainActivity registered receiver and going to read step sensor")
        setAlarm(CHECK_INTERVAL_MIN)
    }

    override fun onResume() {
        Log.d(TAG, "MainActivity onResume called")
        super.onResume()
    }

    private fun calculateNextAlarmTime(intervalMinutes: Long): Long {
        val now = LocalDateTime.now()
        var temp = now.withHour(startOfActivePeriod).withMinute(0).withSecond(0)
        while (!temp.isAfter(now)) {
            Log.d(TAG, "tried out ${temp.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()}")
            temp = temp.plusMinutes(intervalMinutes)
        }
        Log.d(TAG, "ended up  with ${temp.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()}")
        return temp.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
    }

    fun setAlarm(timeInMinutes: Long) {
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (mAlarmManager == null) {
            Log.e(TAG, "mAlarmManger was not set while trying to set an alarm!")
            return
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, 0)
        val alarmTime = calculateNextAlarmTime(timeInMinutes)
        Log.d(TAG, "Setting new alarm to $alarmTime ($timeInMinutes)")
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
        val now = LocalDateTime.now()
        // do not send notifications outside of active time
        if (now < LocalDateTime.now()
                        .withHour(startOfActivePeriod)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusMinutes(CHECK_INTERVAL_MIN - 1)) {
                            return
        }
        if (now.hour > endOfActivePeriod || (now.hour == endOfActivePeriod && now.minute > 5)) return

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

    fun setActivePeriod(start: Int, end: Int) {
        if ((start in 0..24) && (end in 0..24)) {
            startOfActivePeriod = start
            endOfActivePeriod = end
            Log.d(TAG, "Setting active period $startOfActivePeriod - $endOfActivePeriod")
        }
    }
}
