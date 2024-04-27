package com.maruf.mmusicplayer.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.core.content.ContextCompat.startActivity
import com.maruf.mmusicplayer.FavouriteActivity
import com.maruf.mmusicplayer.PlayerActivity
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Utils {
  fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds =
        (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
  }

  @SuppressLint("SuspiciousIndentation")
  fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
  }

  fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeat) {
      if (increment) {
        if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
            PlayerActivity.songPosition = 0
        else ++PlayerActivity.songPosition
      } else {
        if (PlayerActivity.songPosition == 0)
            PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
        else --PlayerActivity.songPosition
      }
    }
  }

  fun exitApplication() {
    if (PlayerActivity.musicService != null) {
      PlayerActivity.musicService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
      PlayerActivity.musicService?.mediaPlayer?.release()
      PlayerActivity.musicService = null
    }
    exitProcess(1)
  }

  fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
      if (id == music.id) {
        PlayerActivity.isFavourite = true
        return index
      }
    }
    return -1
  }

  fun sendIntent(context: Context, ref: String, pos: Int) {
    val intent = Intent(context, PlayerActivity::class.java)
    intent.putExtra("index", pos)
    intent.putExtra("class", ref)
    startActivity(context, intent, null)
  }
}
