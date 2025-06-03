package com.example.cdd

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

            // TODO: 启动游戏逻辑
            Toast.makeText(this, "开始游戏：$playerName vs AI (${aiStrategy.name})", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class AIStrategyType {
    BASIC,
    ADVANCED,
    EXPERT
} 