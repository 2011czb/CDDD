package UI.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Network.NetworkManager
import com.example.cdd.R

class MultiplayerActivity : AppCompatActivity() {
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"

        val connectionTypeGroup = findViewById<RadioGroup>(R.id.connectionTypeGroup)
        val roomNameInput = findViewById<EditText>(R.id.roomNameInput)
        val connectButton = findViewById<Button>(R.id.connectButton)

        networkManager = NetworkManager.getInstance()

        connectButton.setOnClickListener {
            val connectionType = when (connectionTypeGroup.checkedRadioButtonId) {
                R.id.createRoom -> "CREATE_ROOM"
                R.id.joinRoom -> "JOIN_ROOM"
                else -> return@setOnClickListener
            }

            val roomName = roomNameInput.text.toString().trim()
            if (roomName.isEmpty()) {
                Toast.makeText(this, "请输入房间名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (connectionType == "CREATE_ROOM") {
                if (!networkManager.createServer(roomName)) {
                    Toast.makeText(this, "创建房间失败", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                if (!networkManager.connectToServer(roomName, playerName)) {
                    Toast.makeText(this, "加入房间失败", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // 启动游戏界面
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("gameMode", "MULTI")
                putExtra("playerName", playerName)
                putExtra("gameRule", gameRule)
                putExtra("connectionType", connectionType)
                putExtra("roomName", roomName)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.disconnect()
    }
}

enum class ConnectionType {
    CREATE_ROOM,
    JOIN_ROOM
} 