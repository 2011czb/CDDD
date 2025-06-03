package com.example.cdd

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MultiplayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"

        val connectionTypeGroup = findViewById<RadioGroup>(R.id.connectionTypeGroup)
        val roomNameInput = findViewById<TextInputEditText>(R.id.roomNameInput)
        val startGameButton = findViewById<Button>(R.id.startGameButton)

        startGameButton.setOnClickListener {
            val connectionType = when (connectionTypeGroup.checkedRadioButtonId) {
                R.id.createRoom -> ConnectionType.CREATE_ROOM
                R.id.joinRoom -> ConnectionType.JOIN_ROOM
                else -> ConnectionType.CREATE_ROOM
            }

            val roomName = roomNameInput.text.toString().trim()
            if (roomName.isEmpty()) {
                Toast.makeText(this, "请输入房间名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: 实现网络连接和游戏逻辑
            Toast.makeText(this, "开始游戏：$playerName 在房间 $roomName", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class ConnectionType {
    CREATE_ROOM,
    JOIN_ROOM
} 