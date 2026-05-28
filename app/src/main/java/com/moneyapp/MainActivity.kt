package com.moneyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.moneyapp.data.local.datastore.ThemePreferences
import com.moneyapp.data.local.db.AppDatabase
import com.moneyapp.presentation.navigation.AppNavGraph
import com.moneyapp.presentation.theme.MoneyAppTheme
import com.moneyapp.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point of the application.
 * Uses @AndroidEntryPoint to enable Hilt injection in this Activity.
 *
 * Menginjeksi [ThemePreferences] via Hilt dan mengumpulkan [ThemeMode]
 * sebagai Compose State untuk diteruskan ke [MoneyAppTheme].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            MoneyAppTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(themePreferences = themePreferences, db = database)
                }
            }
        }
    }
}
