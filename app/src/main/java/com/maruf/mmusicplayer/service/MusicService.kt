package com.maruf.mmusicplayer.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.maruf.mmusicplayer.ApplicationClass
import com.maruf.mmusicplayer.NotificationReceiver
import com.maruf.mmusicplayer.PlayerActivity
import com.maruf.mmusicplayer.PlayerActivity.Companion.binding
import com.maruf.mmusicplayer.PlayerActivity.Companion.musicService
import com.maruf.mmusicplayer.R
import com.maruf.mmusicplayer.util.Utils
import com.maruf.mmusicplayer.util.Utils.getImgArt

class MusicService : Service() {
  private val myBinder = MyBinder()
  var mediaPlayer: MediaPlayer? = null
  private lateinit var mediaSession: MediaSessionCompat
  private lateinit var runnable: Runnable

  override fun onBind(p0: Intent?): IBinder? {
    mediaSession = MediaSessionCompat(baseContext, "My Music")
    return myBinder
  }

  inner class MyBinder : Binder() {
    fun currentService(): MusicService {
      return this@MusicService
    }
  }

  @SuppressLint("ForegroundServiceType")
  fun showNotification(playPauseBtn: Int) {

    val preIntent =
        Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
    val prePendingIntent =
        PendingIntent.getBroadcast(baseContext, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val playIntent =
        Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
    val playPendingIntent =
        PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val nextIntent =
        Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
    val nextPendingIntent =
        PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val exitIntent =
        Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
    val exitPendingIntent =
        PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
    val image =
        if (imgArt != null) BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        else BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen)

    val notification =
        NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", prePendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .build()
    startForeground(13, notification)
  }

  fun createMediaPlayer() {
    try {
      if (mediaPlayer == null) mediaPlayer = MediaPlayer()
      mediaPlayer?.apply {
        reset()
        setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        prepare()
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        binding.tvSeekBarStart.text = Utils.formatDuration(mediaPlayer?.currentPosition!!.toLong())
        binding.tvSeekBarEnd.text = Utils.formatDuration(mediaPlayer?.duration!!.toLong())
        binding.seekBarPA.apply {
          progress = 0
          max = mediaPlayer?.duration!!
        }
          PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
      }
    } catch (e: Exception) {
      return
    }
  }

  fun seekBarSetup() {
      runnable = Runnable {
          binding.tvSeekBarStart.text = Utils.formatDuration(mediaPlayer?.currentPosition!!.toLong())
          binding.seekBarPA.progress = mediaPlayer?.currentPosition!!
          Handler(Looper.getMainLooper()).postDelayed(runnable,200)
      }
      Handler(Looper.getMainLooper()).postDelayed(runnable,0)
  }
}
