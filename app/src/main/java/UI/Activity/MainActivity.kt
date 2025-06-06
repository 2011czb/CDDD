package UI.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cdd.R
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_GAME_MODE = "game_mode"
        private const val KEY_GAME_RULE = "game_rule"
        private const val KEY_AI_STRATEGY = "ai_strategy"
    }

    private lateinit var gameModeGroup: RadioGroup
    private lateinit var gameRuleGroup: RadioGroup
    private lateinit var aiStrategyGroup: RadioGroup
    private lateinit var playerNameInput: TextInputEditText
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            Log.d(TAG, "onCreate: 初始化主界面")

            // 初始化视图
            initViews()

            // 设置监听器
            setupListeners()

            // 恢复状态
            if (savedInstanceState != null) {
                restoreState(savedInstanceState)
            }
        } catch (e: Exception) {
            Log.e(TAG, "初始化错误", e)
            Toast.makeText(this, "初始化失败，请重试", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        gameModeGroup = findViewById(R.id.gameModeGroup)
        gameRuleGroup = findViewById(R.id.gameRuleGroup)
        aiStrategyGroup = findViewById(R.id.aiStrategyGroup)
        playerNameInput = findViewById(R.id.playerNameInput)
        startButton = findViewById(R.id.startButton)
    }

    private fun setupListeners() {
        // 游戏模式切换监听
        gameModeGroup.setOnCheckedChangeListener { _, checkedId ->
            val isSingleMode = checkedId == R.id.modeSingle
            aiStrategyGroup.isEnabled = isSingleMode
            Log.d(TAG, "游戏模式切换: ${if (isSingleMode) "单人模式" else "多人模式"}")
        }

        // 开始游戏按钮点击监听
        startButton.setOnClickListener {
            try {
                val playerName = playerNameInput.text.toString().trim()
                if (playerName.isEmpty()) {
                    Toast.makeText(this, "请输入玩家名称", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val isSingleMode = gameModeGroup.checkedRadioButtonId == R.id.modeSingle
                val isNorthRule = gameRuleGroup.checkedRadioButtonId == R.id.ruleNorth
                val aiStrategy = when (aiStrategyGroup.checkedRadioButtonId) {
                    R.id.strategyBasic -> "basic"
                    R.id.strategyAdvanced -> "advanced"
                    R.id.strategyExpert -> "expert"
                    else -> "basic"
                }

                val intent = Intent(
                    this,
                    if (isSingleMode) SinglePlayerActivity::class.java else MultiplayerActivity::class.java
                )
                intent.putExtra("player_name", playerName)
                intent.putExtra("game_rule", if (isNorthRule) "north" else "south")
                intent.putExtra("ai_strategy", aiStrategy)
                startActivity(intent)
                Log.d(TAG, "开始游戏: 模式=${if (isSingleMode) "单人" else "多人"}, 规则=${if (isNorthRule) "北方" else "南方"}")
            } catch (e: Exception) {
                Log.e(TAG, "开始游戏失败", e)
                Toast.makeText(this, "启动游戏失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putString(KEY_PLAYER_NAME, playerNameInput.text.toString())
            outState.putInt(KEY_GAME_MODE, gameModeGroup.checkedRadioButtonId)
            outState.putInt(KEY_GAME_RULE, gameRuleGroup.checkedRadioButtonId)
            outState.putInt(KEY_AI_STRATEGY, aiStrategyGroup.checkedRadioButtonId)
            Log.d(TAG, "保存状态")
        } catch (e: Exception) {
            Log.e(TAG, "保存状态失败", e)
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        try {
            playerNameInput.setText(savedInstanceState.getString(KEY_PLAYER_NAME, ""))
            gameModeGroup.check(savedInstanceState.getInt(KEY_GAME_MODE, R.id.modeSingle))
            gameRuleGroup.check(savedInstanceState.getInt(KEY_GAME_RULE, R.id.ruleNorth))
            aiStrategyGroup.check(savedInstanceState.getInt(KEY_AI_STRATEGY, R.id.strategyBasic))
            Log.d(TAG, "恢复状态")
        } catch (e: Exception) {
            Log.e(TAG, "恢复状态失败", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 主界面销毁")
    }
}