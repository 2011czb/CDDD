package com.example.cdd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Game.Game
import AI.AIStrategyType

class SinglePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"
        val aiStrategy = intent.getStringExtra("aiStrategy") ?: "BASIC"

        // 初始化游戏
        val ruleType = if (gameRule == "NORTH") Game.RULE_NORTH else Game.RULE_SOUTH
        val aiStrategyType = when (aiStrategy) {
            "BASIC" -> AIStrategyType.SMART1
            "ADVANCED" -> AIStrategyType.SMART2
            "EXPERT" -> AIStrategyType.SMART3
            else -> AIStrategyType.SMART1
        }

        // 创建游戏实例
        val game = Game.createSinglePlayerGame(playerName, ruleType, aiStrategyType)
        game.initGame()

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

enum class AIStrategyType {
    SMART1,
    SMART2,
    SMART3
} 