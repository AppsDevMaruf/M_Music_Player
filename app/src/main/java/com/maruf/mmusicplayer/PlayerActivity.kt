package com.maruf.mmusicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import coil.Coil.reset
import coil.load
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityPlayerBinding
import com.maruf.mmusicplayer.service.MusicService
import kotlinx.coroutines.NonCancellable.start

class PlayerActivity : AppCompatActivity(), ServiceConnection {
  private lateinit var binding: ActivityPlayerBinding

  companion object {
    private lateinit var musicListPA: ArrayList<Music>
    private var songPosition = 0
    private var isSongPlaying: Boolean = false
    private var musicService: MusicService? = null
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.coolPink)
    binding = ActivityPlayerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    // start service
    val intent = Intent(this, MusicService::class.java)
    bindService(intent, this, BIND_AUTO_CREATE)
    startService(intent)

    initializeLayout()
    binding.apply {
      playPauseBtnPA.setOnClickListener { if (isSongPlaying) pauseMusic() else playMusic() }
      previousBtnPA.setOnClickListener { nextOrPreviousSong(false) }
      nextBtnPA.setOnClickListener { nextOrPreviousSong(true) }
    }
  }

  private fun initializeLayout() {
    songPosition = intent.getIntExtra("index", 0)
    when (intent.getStringExtra("class")) {
      "MusicAdapter" -> {
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.MusicListMA)
        setLayout()

      }
      "MainActivity" -> {
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.MusicListMA)
        musicListPA.shuffle()
        setLayout()
      }
    }
  }

  private fun setLayout() {
    binding.songImgPA.load(musicListPA[songPosition].artUri) {
      crossfade(true)
      placeholder(R.mipmap.ic_music_player_icon)
      error(R.mipmap.ic_music_player_icon)
    }
    binding.songNamePA.text = musicListPA[songPosition].title
  }

  private fun createMediaPlayer() {
    try {
      if (musicService?.mediaPlayer == null) musicService?.mediaPlayer = MediaPlayer()
      musicService?.mediaPlayer?.apply {
        reset()
        setDataSource(musicListPA[songPosition].path)
        prepare()
        start()
        isSongPlaying = true
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
      }
    } catch (e: Exception) {
      return
    }
  }

  private fun playMusic() {
    binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
    isSongPlaying = true
    musicService?.mediaPlayer?.start()
  }

  private fun pauseMusic() {
    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    isSongPlaying = false
    musicService?.mediaPlayer?.pause()
  }

  private fun nextOrPreviousSong(increment: Boolean) {
    if (increment) {
      if (musicListPA.size - 1 == songPosition) songPosition = 0 else ++songPosition
    } else {
      if (songPosition == 0) songPosition = musicListPA.size - 1 else --songPosition
    }
    setLayout()
    createMediaPlayer()
  }

  override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
    val binder = service as MusicService.MyBinder
    musicService = binder.currentService()
    createMediaPlayer()
  }

  override fun onServiceDisconnected(p0: ComponentName?) {
    musicService = null
  }
}
