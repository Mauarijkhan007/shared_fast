package com.example.myapplication

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logoImage)
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        logo.startAnimation(animation)

        mediaPlayer = MediaPlayer.create(this, R.raw.into)
        mediaPlayer.start()

        Handler(Looper.getMainLooper()).postDelayed({
            if (::mediaPlayer.isInitialized) {
                try {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                } catch (_: IllegalStateException) {
                    // It's okay if already stopped or not in a good state
                }

                try {
                    mediaPlayer.release()
                } catch (_: Exception) {
                    // Same here
                }
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)


    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            try {
                mediaPlayer.stop()
            } catch (_: IllegalStateException) {
                // Already stopped or released, ignore
            }
            try {
                mediaPlayer.release()
            } catch (_: Exception) {
                // Safe guard for any other unexpected state
            }
        }
    }

}
