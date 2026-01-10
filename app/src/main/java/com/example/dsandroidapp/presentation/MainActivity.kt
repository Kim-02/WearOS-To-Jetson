/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.dsandroidapp.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.dsandroidapp.presentation.theme.DSAndroidAppTheme

class MainActivity : ComponentActivity() {

    // 사용할 권한들
    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.INTERNET
    )

    // 권한 요청 결과 처리
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.all {it.value}) {
            // 모든 권한 허용 시 서비스 시작
            startSensorService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 체크 후 없으면 요청, 있으면 서비스 시작
        if (hasPermissions()) {
            startSensorService()
        } else {
            requestPermissionLauncher.launch(permissions)
        }

        setContent {
            WearApp("센서 모니터링")
        }
    }

    private fun hasPermissions() : Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startSensorService() {
        val intent = Intent(this, SensorDataService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // 포그라운드 서비스 전용 시작 명령
        } else {
            startService(intent)
        }
    }
}

// UI 정의 부분 (WearApp Unresolved 에러 해결)
@Composable
fun WearApp (greetingName: String){
    DSAndroidAppTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "상태: $greetingName"
    )
}