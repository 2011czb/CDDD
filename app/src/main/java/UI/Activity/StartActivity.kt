package UI.Activity

import UI.Music.MusicManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.cdd.R

class StartActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "StartActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        Log.d(TAG, "onCreate: 启动音乐服务")
        // 启动背景音乐
        MusicManager.Companion.getInstance().startMusic(this)

        // 开始游戏按钮
        findViewById<Button>(R.id.startGameButton).setOnClickListener {
            Log.d(TAG, "点击开始游戏按钮")
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 游戏设置按钮
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            Log.d(TAG, "点击设置按钮")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 退出游戏按钮
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            Log.d(TAG, "点击退出按钮，释放音乐资源")
            MusicManager.Companion.getInstance().release(this)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: 暂停音乐")
        MusicManager.Companion.getInstance().pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: 恢复音乐")
        MusicManager.Companion.getInstance().resumeMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 释放音乐资源")
        MusicManager.Companion.getInstance().release(this)
    }
}