package com.example.cdd

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"

        val testTypeGroup = findViewById<RadioGroup>(R.id.testTypeGroup)
        val startTestButton = findViewById<Button>(R.id.startTestButton)

        startTestButton.setOnClickListener {
            val testType = when (testTypeGroup.checkedRadioButtonId) {
                R.id.testHumanVsAI -> TestType.HUMAN_VS_AI
                R.id.testGameInit -> TestType.GAME_INIT
                R.id.testManualDeal -> TestType.MANUAL_DEAL
                else -> TestType.HUMAN_VS_AI
            }

            // TODO: 实现测试逻辑
            Toast.makeText(this, "开始测试：$playerName - ${testType.name}", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class TestType {
    HUMAN_VS_AI,
    GAME_INIT,
    MANUAL_DEAL
} 