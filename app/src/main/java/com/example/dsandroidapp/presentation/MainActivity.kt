package com.example.dsandroidapp.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import com.example.dsandroidapp.presentation.theme.DSAndroidAppTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.BODY_SENSORS] == true) {
            Log.d("DS_MAIN", "✅ 심박수 권한 허용됨")
            startSensorService()
        } else {
            Log.e("DS_MAIN", "❌ 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 체크 후 없으면 팝업 요청
        if (requiredPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            startSensorService()
        } else {
            requestPermissionLauncher.launch(requiredPermissions)
        }

        setContent {
            DSAndroidAppTheme {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {
                    Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, text = "DS 수집기 작동 중")
                }
            }
        }
    }

    private fun startSensorService() {
        val intent = Intent(this, SensorDataService::class.java)
        startForegroundService(intent)
    }
}