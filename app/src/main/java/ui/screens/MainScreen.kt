 package com.example.cdd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    onStartGame: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 游戏标题
            Text(
                text = "锄大地",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 开始游戏按钮
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text("开始游戏", fontSize = 20.sp)
            }
            
            // 设置按钮
            Button(
                onClick = onSettings,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text("设置", fontSize = 20.sp)
            }
            
            // 退出游戏按钮
            Button(
                onClick = onExit,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text("退出游戏", fontSize = 20.sp)
            }
        }
    }
}