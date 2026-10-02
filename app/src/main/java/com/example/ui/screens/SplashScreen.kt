package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackgroundNavy
import com.example.ui.theme.DarkCanvasBg
import com.example.ui.theme.RailwayAmber
import com.example.ui.theme.RailwayGold
import com.example.ui.theme.RailwayGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Initializing SECR Kharsia Lobby...") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        alpha.animateTo(1f, animationSpec = tween(600))
        
        statusText = "Loading 369 Kharsia Running Staff..."
        progress = 0.35f
        delay(400)
        
        statusText = "Loading 141 SECR Station CUG Contacts..."
        progress = 0.70f
        delay(400)
        
        statusText = "Opening 24x7 Operations Desk..."
        progress = 1f
        delay(350)
        
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF061426), Color(0xFF0A1E3D), Color(0xFF030A14))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background watermark
        Image(
            painter = painterResource(id = R.drawable.bg_kharsia_lobby_building),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.12f
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(28.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Glowing Logo Container with KHARSIA LOBBY Emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(164.dp)
                    .scale(glowScale)
            ) {
                // Outer glow halo ring
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(RailwayGold.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )

                // Master Logo with KHARSIA LOBBY in clear bold lettering
                Image(
                    painter = painterResource(id = R.drawable.ic_kharsia_lobby_logo),
                    contentDescription = "KHARSIA LOBBY Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(142.dp)
                        .clip(CircleShape)
                        .border(3.dp, RailwayGold, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // BIG BOLD App Title
            Text(
                text = "KHARSIA LOBBY",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = RailwayGold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "खरसिया लॉबी",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SOUTH EAST CENTRAL RAILWAY • BILASPUR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF93C5FD),
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(34.dp))

            // Loading indicator & status
            Column(
                modifier = Modifier.width(260.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RailwayGold,
                    trackColor = Color(0xFF1E3A8A)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = statusText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA0B4D0),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom version note
        Text(
            text = "24x7 Integrated Crew & Lobby Management Portal",
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
