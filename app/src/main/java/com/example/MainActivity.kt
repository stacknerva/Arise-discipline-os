package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.NotificationHelper
import com.example.ui.AppNavigation
import com.example.ui.DisciplineViewModel
import com.example.ui.DisciplineViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.data.QuoteApiService

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: DisciplineViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dpaste.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val apiService = retrofit.create(QuoteApiService::class.java)
        
        val timeRetrofit = Retrofit.Builder()
            .baseUrl("https://worldtimeapi.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val timeApiService = timeRetrofit.create(com.example.data.WorldTimeApiService::class.java)

        val app = application as DisciplineApplication
        val factory = DisciplineViewModelFactory(
            app.repository,
            app.settingsRepository,
            NotificationHelper(this),
            apiService,
            timeApiService
        )
        viewModel = ViewModelProvider(this, factory)[DisciplineViewModel::class.java]

        setContent {
            MyApplicationTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}
