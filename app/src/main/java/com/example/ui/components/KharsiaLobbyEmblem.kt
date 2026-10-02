package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun KharsiaLobbyEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_kharsia_lobby_logo),
        contentDescription = "Kharsia Lobby SECR Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}
