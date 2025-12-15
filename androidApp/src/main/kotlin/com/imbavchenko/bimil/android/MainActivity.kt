package com.imbavchenko.bimil.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.imbavchenko.bimil.di.ActivityProvider
import com.imbavchenko.bimil.presentation.BimilApp
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val activityProvider: ActivityProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    BimilApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        activityProvider.clearActivity()
    }
}
