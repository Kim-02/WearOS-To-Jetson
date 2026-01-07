package com.example.dsandroidapp.presentation

import android.app.Service
import android.os.IBinder
import android.content.Intent

class SensorDataService : Service() {

    // 서비스를 시작할 때 호출됨
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 여기서 포그라운드 서비스 알림을 띄우고 센서 수집을 시작할 예정입니다.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}