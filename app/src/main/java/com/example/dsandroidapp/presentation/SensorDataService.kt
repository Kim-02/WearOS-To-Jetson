package com.example.dsandroidapp.presentation

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class SensorDataService : Service(), SensorEventListener {

    private val tag = "SensorDataService"
    private val channelID = "SensorServiceChannel"
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var bodyTemperSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // 1. 센서 매니저 및 심박수 센서 초기화
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        bodyTemperSensor = sensorManager.getDefaultSensor(69686)

        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        deviceSensors.forEach { sensor ->
            Log.d(tag, "찾은 센서: ${sensor.name}, 타입 번호: ${sensor.type}")
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 2. 포그라운드 서비스 알림 띄우기
        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("실시간 생체 데이터 수집 중")
            .setContentText("심박수 데이터를 측정하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)

        // 3. 센서 리스너 등록 (데이터 수집 시작)
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(tag, "심박수 센서 리스너 등록 성공")
        }

        bodyTemperSensor?.let{
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(tag, "체온 센서 리스너 등록 성공")
        }

        return START_STICKY
    }

    // 4. 센서 값이 변할 때마다 호출되는 함수
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type){
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0]
                Log.d(tag, "심박수: $heartRate bpm")
            }
            69686 -> {
                val temperature = event.values[0]
                Log.d(tag, "체온: $temperature ℃")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this) // 서비스 종료 시 센서 해제
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelID, "Sensor Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}