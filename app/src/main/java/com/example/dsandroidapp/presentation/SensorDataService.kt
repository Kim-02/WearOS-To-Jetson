package com.example.dsandroidapp.presentation

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo // <- Unresolved 에러 해결
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import android.provider.Settings
import android.Manifest
import android.content.pm.PackageManager
import java.util.Locale
import java.util.*
import java.text.SimpleDateFormat

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

@Serializable
data class SensorPayload(val id: String, val timestamp: String, val hr: Float)

class SensorDataService : Service(), SensorEventListener {

    private val tag = "DS_SERVICE"
    private val channelID = "SensorServiceChannel"
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var lastUpdateTime: Long = 0
    private var currentHeartRate: Float = 0f

    private val client = HttpClient(CIO) { install(ContentNegotiation) { json() } }
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("DS 실시간 전송")
            .setContentText("심박수 데이터 전송 중 (192.168.0.12)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(tag, "✅ 센서 등록 완료")
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            currentHeartRate = event.values[0]
            Log.d(tag, "💓 심박수: $currentHeartRate")
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime >= 5000) {
            lastUpdateTime = currentTime
            sendToJetson(currentHeartRate)
        }
    }

    private fun sendToJetson(hrValue: Float) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val time = sdf.format(Date())
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        serviceScope.launch {
            try {
                client.post("http://192.168.0.12:5000/data") {
                    contentType(ContentType.Application.Json)
                    setBody(SensorPayload(deviceId, time, hrValue))
                }
                Log.i(tag, "🚀 전송 성공: $hrValue")
            } catch (e: Exception) {
                Log.e(tag, "📡 전송 실패: ${e.message}")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sensorManager.unregisterListener(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(channelID, "DS", NotificationManager.IMPORTANCE_LOW))
        }
    }
}