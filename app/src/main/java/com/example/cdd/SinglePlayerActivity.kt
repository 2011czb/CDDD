package com.example.cdd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SinglePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"

        val aiStrategyGroup = findViewById<RadioGroup>(R.id.aiStrategyGroup)
        val startGameButton = findViewById<Button>(R.id.startGameButton)

        startGameButton.setOnClickListener {
            val aiStrategy = when (aiStrategyGroup.checkedRadioButtonId) {
                R.id.strategyBasic -> AIStrategyType.BASIC
                R.id.strategyAdvanced -> AIStrategyType.ADVANCED
                R.id.strategyExpert -> AIStrategyType.EXPERT
                else -> AIStrategyType.BASIC
            }

            // 跳转到游戏界面
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("playerName", playerName)
                putExtra("gameRule", gameRule)
                putExtra("gameMode", "SINGLE")
                putExtra("aiStrategy", aiStrategy.name)
            }
            startActivity(intent)
        }
    }
}

enum class AIStrategyType {
    BASIC,
    ADVANCED,
    EXPERT
} 