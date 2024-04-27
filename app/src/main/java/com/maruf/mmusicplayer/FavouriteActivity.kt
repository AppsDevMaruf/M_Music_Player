package com.maruf.mmusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maruf.mmusicplayer.adapter.FavouriteAdapter
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
  private lateinit var binding: ActivityFavouriteBinding
  private lateinit var favouriteAdapter: FavouriteAdapter

  companion object {
    var favouriteSong: ArrayList<Music> = ArrayList()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.coolPink)
    binding = ActivityFavouriteBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.backBtnFA.setOnClickListener { finish() }
    initializeLayout()
  }

  private fun initializeLayout() {
    favouriteAdapter = FavouriteAdapter(this@FavouriteActivity, favouriteSong)
    binding.favouriteRV.apply {
      setHasFixedSize(true)
      setItemViewCacheSize(13)
      adapter = favouriteAdapter
    }
  }
}
