package com.maruf.mmusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maruf.mmusicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
  private lateinit var binding: ActivityFavouriteBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.Theme_MMusicPlayer)
    binding = ActivityFavouriteBinding.inflate(layoutInflater)
    setContentView(R.layout.activity_favourite)
  }
}