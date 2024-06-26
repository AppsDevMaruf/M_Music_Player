package com.maruf.mmusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maruf.mmusicplayer.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {
  private lateinit var binding:ActivityPlaylistBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.coolPink)
    binding = ActivityPlaylistBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.backBtnPLA.setOnClickListener { finish() }

  }
}
