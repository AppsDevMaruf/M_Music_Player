package com.maruf.mmusicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.maruf.mmusicplayer.adapter.FavouriteAdapter
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityFavouriteBinding
import com.maruf.mmusicplayer.util.Utils.sendIntent

class FavouriteActivity : AppCompatActivity() {
  private lateinit var binding: ActivityFavouriteBinding

  companion object {
    var favouriteSongs: ArrayList<Music> = ArrayList()
    @SuppressLint("StaticFieldLeak")
    lateinit var favouriteAdapter: FavouriteAdapter
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTheme(R.style.coolPink)
    binding = ActivityFavouriteBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initializeLayout()
    binding.apply {
      backBtnFA.setOnClickListener { finish() }
      shuffleBtnFA.setOnClickListener {
        sendIntent(this@FavouriteActivity,"FavouriteShuffle",0)
      }
    }


  }

  private fun initializeLayout() {
    if (favouriteSongs.size<1) binding.shuffleBtnFA.visibility = View.INVISIBLE
    favouriteAdapter = FavouriteAdapter(this@FavouriteActivity, favouriteSongs)
    binding.favouriteRV.apply {
      setHasFixedSize(true)
      setItemViewCacheSize(13)
      adapter = favouriteAdapter
    }
  }
}
