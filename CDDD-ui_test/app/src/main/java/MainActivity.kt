package com.example.cdd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import AI.AIStrategyType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameModeGroup = findViewById<RadioGroup>(R.id.gameModeGroup)
        val gameRuleGroup = findViewById<RadioGroup>(R.id.gameRuleGroup)
        val aiStrategyGroup = findViewById<RadioGroup>(R.id.aiStrategyGroup)
        val playerNameInput = findViewById<TextInputEditText>(R.id.playerNameInput)
        val startButton = findViewById<Button>(R.id.startButton)

        startButton.setOnClickListener {
            val playerName = playerNameInput.text.toString().trim()
            if (playerName.isEmpty()) {
                Toast.makeText(this, "请输入玩家名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gameRule = when (gameRuleGroup.checkedRadioButtonId) {
                R.id.ruleNorth -> "NORTH"
                R.id.ruleSouth -> "SOUTH"
                else -> "NORTH"
            }

            val aiStrategy = when (aiStrategyGroup.checkedRadioButtonId) {
                R.id.strategyBasic -> "BASIC"
                R.id.strategyAdvanced -> "ADVANCED"
                R.id.strategyExpert -> "EXPERT"
                else -> "BASIC"
            }

            val intent = when (gameModeGroup.checkedRadioButtonId) {
                R.id.modeSingle -> Intent(this, SinglePlayerActivity::class.java)
                R.id.modeMulti -> Intent(this, MultiplayerActivity::class.java)
                else -> return@setOnClickListener
            }

            intent.putExtra("playerName", playerName)
            intent.putExtra("gameRule", gameRule)
            intent.putExtra("aiStrategy", aiStrategy)
            startActivity(intent)
        }
    }
}

enum class GameMode {
    SINGLE_PLAYER,
    MULTIPLAYER
}

enum class GameRule {
    NORTH,
    SOUTH
}

enum class AIStrategy {
    BASIC,
    ADVANCED,
    EXPERT
}