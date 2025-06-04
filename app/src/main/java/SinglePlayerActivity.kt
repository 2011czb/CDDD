package com.example.cdd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SinglePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"
        val aiStrategy = intent.getStringExtra("aiStrategy") ?: "BASIC"

        // 启动游戏界面
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("gameMode", "SINGLE")
            putExtra("playerName", playerName)
            putExtra("gameRule", gameRule)
            putExtra("aiStrategy", aiStrategy)
        }
        startActivity(intent)
        finish()
    }
}