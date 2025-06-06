package UI.Music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class MusicManager private constructor() {
    private var musicService: MusicService? = null
    private var isBound = false
    private var isPaused = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            musicService = null
            isBound = false
        }
    }

    companion object {
        @Volatile
        private var instance: MusicManager? = null

        fun getInstance(): MusicManager {
            return instance ?: synchronized(this) {
                instance ?: MusicManager().also { instance = it }
            }
        }
    }

    fun startMusic(context: Context) {
        try {
            if (!isBound) {
                val intent = Intent(context, MusicService::class.java)
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error starting music service: ${e.message}")
        }
    }

    fun pauseMusic() {
        try {
            if (isBound) {
                musicService?.let {
                    // 服务会继续运行，但音乐暂停
                    isPaused = true
                }
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error pausing music: ${e.message}")
        }
    }

    fun resumeMusic() {
        try {
            if (isBound) {
                musicService?.let {
                    isPaused = false
                }
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error resuming music: ${e.message}")
        }
    }

    fun setVolume(volume: Int) {
        try {
            if (isBound) {
                musicService?.setVolume(volume)
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error setting volume: ${e.message}")
        }
    }

    fun getVolume(): Int {
        return try {
            if (isBound) {
                musicService?.getVolume() ?: 50
            } else {
                50
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error getting volume: ${e.message}")
            50
        }
    }

    fun release(context: Context) {
        try {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
            context.stopService(Intent(context, MusicService::class.java))
        } catch (e: Exception) {
            Log.e("MusicManager", "Error releasing music manager: ${e.message}")
        }
    }
}