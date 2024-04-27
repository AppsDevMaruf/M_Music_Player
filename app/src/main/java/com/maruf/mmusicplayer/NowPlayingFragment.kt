package com.maruf.mmusicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import coil.load
import com.maruf.mmusicplayer.databinding.FragmentNowPlayingBinding
import com.maruf.mmusicplayer.util.Utils

class NowPlayingFragment : Fragment() {
  @SuppressLint("StaticFieldLeak")
  companion object {
    lateinit var binding: FragmentNowPlayingBinding
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
    binding = FragmentNowPlayingBinding.bind(view)
    binding.root.visibility = View.INVISIBLE
    binding.playPauseBtnNP.setOnClickListener {
      if (PlayerActivity.isSongPlaying) pauseMusic() else playMusic()
    }
    binding.nextBtnNP.setOnClickListener {
      nextMusic()
    }
    binding.root.setOnClickListener {
      val intent = Intent(requireContext(), PlayerActivity::class.java)
      intent.putExtra("index",PlayerActivity.songPosition)
      intent.putExtra("class","NowPlaying")
      ContextCompat.startActivity(requireContext(), intent, null)
    }
    return view
  }

  override fun onResume() {
    super.onResume()
    if (PlayerActivity.musicService != null) {
      binding.root.visibility = View.VISIBLE
      binding.songNameNP.isSelected = true
      binding.songImgNP.load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri) {
        crossfade(true)
        placeholder(R.mipmap.ic_music_player_icon)
        error(R.mipmap.ic_music_player_icon)
      }
      binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
      if (PlayerActivity.isSongPlaying)
          binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
      else binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    }
  }

  private fun playMusic() {
    PlayerActivity.isSongPlaying = true
    PlayerActivity.musicService!!.mediaPlayer!!.start()
    binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
  }

  private fun pauseMusic() {
    PlayerActivity.isSongPlaying = false
    PlayerActivity.musicService!!.mediaPlayer!!.pause()
    binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
  }

  private fun nextMusic() {
    Utils.setSongPosition(true)
    PlayerActivity.musicService?.createMediaPlayer()
    binding.songImgNP.load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri) {
      crossfade(true)
      placeholder(R.mipmap.ic_music_player_icon)
      error(R.mipmap.ic_music_player_icon)
    }
    binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
    PlayerActivity.musicService?.showNotification(R.drawable.pause_icon)
    playMusic()

  }
}
