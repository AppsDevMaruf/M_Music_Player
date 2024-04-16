package com.maruf.mmusicplayer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPlayerBinding

  companion object {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var musicListPA: ArrayList<Music>
    private var songPosition = 0
    private var isSongPlaying: Boolean = false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.coolPink)
    binding = ActivityPlayerBinding.inflate(layoutInflater)
    setContentView(binding.root)
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
      }
      "MainActivity" -> {
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.MusicListMA)
        musicListPA.shuffle()
        setLayout()
        createMediaPlayer()
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
      if (mediaPlayer == null) mediaPlayer = MediaPlayer()
      mediaPlayer?.apply {
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
    mediaPlayer?.start()
  }

  private fun pauseMusic() {
    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    isSongPlaying = false
    mediaPlayer?.pause()
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
}
