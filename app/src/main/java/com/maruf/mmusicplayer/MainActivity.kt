package com.maruf.mmusicplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.maruf.mmusicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //enableEdgeToEdge()
    setTheme(R.style.Theme_MMusicPlayer)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.shuffleBtn.setOnClickListener {
      Toast.makeText(this@MainActivity, "a", Toast.LENGTH_SHORT).show()
    }
    binding.apply {
      shuffleBtn.setOnClickListener { startActivity(Intent(this@MainActivity, PlayerActivity::class.java)) }
      playlistBtn.setOnClickListener { startActivity(Intent(this@MainActivity, PlaylistActivity::class.java)) }
      favouriteBtn.setOnClickListener { startActivity(Intent(this@MainActivity, FavouriteActivity::class.java)) }
    }
  }
}
