package com.maruf.mmusicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.maruf.mmusicplayer.adapter.MusicAdapter
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var toggle: ActionBarDrawerToggle
  private lateinit var musicAdapter: MusicAdapter

  companion object {
    lateinit var MusicListMA: ArrayList<Music>
    var sortOrder: Int = 0
    val sortingList = arrayOf(Media.DATE_ADDED + " DESC", Media.TITLE, Media.SIZE + " DESC")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // enableEdgeToEdge()
    requestRuntimePermission()
    setTheme(R.style.coolPinkNav)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    // for nav drawer
    toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
    binding.root.addDrawerListener(toggle)
    toggle.syncState()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.show()
    if (requestRuntimePermission()) initializeLayout()

    binding.apply {
      shuffleBtn.setOnClickListener {
        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
        intent.putExtra("index", 0)
        intent.putExtra("class", "MainActivity")
        startActivity(intent)
      }
      playlistBtn.setOnClickListener {
        startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
      }
      favouriteBtn.setOnClickListener {
        startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))
      }
    }
  }

  @SuppressLint("StringFormatMatches")
  private fun initializeLayout() {
    // adapter initialize
    MusicListMA = getAllAudio()
    musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
    binding.musicRV.apply {
      setHasFixedSize(true)
      setItemViewCacheSize(13)
      adapter = musicAdapter
    }
    binding.totalSongs.text = getString(R.string.total_songs_count, musicAdapter.itemCount)
  }

  private fun requestRuntimePermission(): Boolean {
    if (ActivityCompat.checkSelfPermission(
        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
      return false
    }
    return true
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 13) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        initializeLayout()
      } else {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (toggle.onOptionsItemSelected(item)) return true
    return super.onOptionsItemSelected(item)
  }

  @SuppressLint("Range")
  private fun getAllAudio(): ArrayList<Music> {
    val tempList = ArrayList<Music>()
    val selection = Media.IS_MUSIC + "!=0"
    val projection =
        arrayOf(
            Media._ID,
            Media.TITLE,
            Media.ALBUM,
            Media.ARTIST,
            Media.DURATION,
            Media.DATE_ADDED,
            Media.DATA,
            Media.ALBUM_ID,
        )
    val cursor =
        this.contentResolver.query(
            Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortingList[sortOrder], null)

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          val titleC = cursor.getString(cursor.getColumnIndex(Media.TITLE)) ?: "Unknown"
          val idC = cursor.getString(cursor.getColumnIndex(Media._ID)) ?: "Unknown"
          val albumC = cursor.getString(cursor.getColumnIndex(Media.ALBUM)) ?: "Unknown"
          val artistC = cursor.getString(cursor.getColumnIndex(Media.ARTIST)) ?: "Unknown"
          val pathC = cursor.getString(cursor.getColumnIndex(Media.DATA))
          val durationC = cursor.getLong(cursor.getColumnIndex(Media.DURATION))
          val albumIdC = cursor.getLong(cursor.getColumnIndex(Media.ALBUM_ID)).toString()
          val uri = Uri.parse("content://media/external/audio/albumart")
          val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
          val music =
              Music(
                  id = idC,
                  title = titleC,
                  album = albumC,
                  artist = artistC,
                  path = pathC,
                  duration = durationC,
                  artUri = artUriC)
          val file = File(music.path)
          if (file.exists()) tempList.add(music)
        } while (cursor.moveToNext())
      }
      cursor.close()
    }
    return tempList
  }
}
