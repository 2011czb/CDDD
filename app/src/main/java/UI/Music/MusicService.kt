package UI.Music

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.cdd.R

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 1.0f
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(currentVolume, currentVolume)
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error creating MediaPlayer: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error starting music: ${e.message}")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun setVolume(volume: Int) {
        try {
            currentVolume = volume / 100f
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (e: Exception) {
            Log.e("MusicService", "Error setting volume: ${e.message}")
        }
    }

    fun getVolume(): Int {
        return (currentVolume * 100).toInt()
    }

    override fun onDestroy() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("MusicService", "Error destroying service: ${e.message}")
        }
        super.onDestroy()
    }
}