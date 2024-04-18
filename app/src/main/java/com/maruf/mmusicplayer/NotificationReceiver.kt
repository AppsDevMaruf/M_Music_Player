package com.maruf.mmusicplayer

import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import coil.load
import com.maruf.mmusicplayer.PlayerActivity.Companion.binding
import com.maruf.mmusicplayer.PlayerActivity.Companion.musicService
import com.maruf.mmusicplayer.util.Utils.exitApplication
import com.maruf.mmusicplayer.util.Utils.setSongPosition
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
  override fun onReceive(content: Context?, intent: Intent?) {
    when (intent?.action) {
      ApplicationClass.PREVIOUS -> nextOrPreviousSong(false)
      ApplicationClass.PLAY -> if (PlayerActivity.isSongPlaying) pauseMusic() else playMusic()
      ApplicationClass.NEXT -> nextOrPreviousSong(true)
      ApplicationClass.EXIT -> {
        exitApplication()
      }
    }
  }

  private fun playMusic() {
    PlayerActivity.apply {
      isSongPlaying = true
      musicService?.mediaPlayer?.start()
      musicService?.showNotification(R.drawable.pause_icon)
      binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
    }
  }

  private fun pauseMusic() {
    PlayerActivity.apply {
      isSongPlaying = false
      musicService?.mediaPlayer?.pause()
      musicService?.showNotification(R.drawable.play_icon)
      binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    }
  }

  private fun nextOrPreviousSong(increment: Boolean) {
    setSongPosition(increment)
    musicService?.createMediaPlayer()

    // for image load
    binding.songImgPA.load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri) {
      crossfade(true)
      placeholder(R.mipmap.ic_music_player_icon)
      error(R.mipmap.ic_music_player_icon)
    }
    binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

    playMusic()
  }
}
