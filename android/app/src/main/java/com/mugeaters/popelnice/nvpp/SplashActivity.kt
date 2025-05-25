package com.mugeaters.popelnice.nvpp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugeaters.popelnice.nvpp.R
import com.mugeaters.popelnice.nvpp.ui.theme.NovaVesOdpadTheme
import kotlinx.coroutines.delay

/**
 * Splash screen activity that displays the app logo and title briefly before launching MainActivity
 */
class SplashActivity : ComponentActivity() {
    
    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NovaVesOdpadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(160.dp)
                        )
                        
                        // Title
                        Text(
                            text = "Popelnice",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                        
                        // Subtitle
                        Text(
                            text = "Nová Ves pod Pleší",
                            fontSize = 17.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
            
            // Navigate to MainActivity after delay
            LaunchedEffect(Unit) {
                delay(SPLASH_DELAY)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}