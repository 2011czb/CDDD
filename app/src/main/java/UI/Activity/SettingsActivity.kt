package UI.Activity

import UI.Music.MusicManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cdd.R

class SettingsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SettingsActivity"
    }

    private lateinit var musicVolumeSeekBar: SeekBar
    private lateinit var musicVolumeText: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Log.d(TAG, "onCreate: 初始化设置界面")

        // 初始化视图
        initViews()

        // 设置监听器
        setupListeners()

        // 加载设置
        loadSettings()
    }

    private fun initViews() {
        musicVolumeSeekBar = findViewById(R.id.musicVolumeSeekBar)
        musicVolumeText = findViewById(R.id.musicVolumeText)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupListeners() {
        // 音乐音量
        musicVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                musicVolumeText.text = "音量: $progress%"
                Log.d(TAG, "音量调节: $progress%")
                MusicManager.Companion.getInstance().setVolume(progress)
                saveSettings()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 返回按钮
        backButton.setOnClickListener {
            Log.d(TAG, "点击返回按钮")
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("GameSettings", MODE_PRIVATE)

        // 加载音乐音量
        val musicVolume = prefs.getInt("music_volume", 50)
        musicVolumeSeekBar.progress = musicVolume
        musicVolumeText.text = "音量: $musicVolume%"
        Log.d(TAG, "加载音量设置: $musicVolume%")
        MusicManager.Companion.getInstance().setVolume(musicVolume)
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("GameSettings", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("music_volume", musicVolumeSeekBar.progress)
            apply()
        }
        Log.d(TAG, "保存音量设置: ${musicVolumeSeekBar.progress}%")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 设置界面销毁")
        // 注意：这里不要调用 release，因为其他 Activity 可能还在使用音乐服务
    }
}