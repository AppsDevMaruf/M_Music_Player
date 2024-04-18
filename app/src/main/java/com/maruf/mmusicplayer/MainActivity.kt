package com.maruf.mmusicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maruf.mmusicplayer.adapter.MusicAdapter
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityMainBinding
import com.maruf.mmusicplayer.util.Utils.exitApplication
import java.io.File

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var toggle: ActionBarDrawerToggle
  private lateinit var musicAdapter: MusicAdapter

  companion object {
    lateinit var MusicListMA: ArrayList<Music>
    lateinit var musicListSearch: ArrayList<Music>
    var search: Boolean = false
    var sortOrder: Int = 0
    val sortingList = arrayOf(Media.DATE_ADDED + " DESC", Media.TITLE, Media.SIZE + " DESC")
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // enableEdgeToEdge()
    setTheme(R.style.coolPinkNav)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    // for nav drawer
    toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
    binding.root.addDrawerListener(toggle)
    toggle.syncState()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.show()
    if (requestRuntimePermissions()) initializeLayout()

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
      binding.navView.setNavigationItemSelectedListener {
        when (it.itemId) {
          R.id.navView -> Toast.makeText(this@MainActivity, "Setting", Toast.LENGTH_SHORT).show()
          R.id.navSettings ->
              Toast.makeText(this@MainActivity, "Setting", Toast.LENGTH_SHORT).show()
          R.id.navAbout -> Toast.makeText(this@MainActivity, "About", Toast.LENGTH_SHORT).show()
          R.id.navExit -> {
            val build = MaterialAlertDialogBuilder(this@MainActivity)
            build
                .setTitle("Exit")
                .setMessage("Do you want to close app?")
                .setPositiveButton("Yes") { _, _ -> exitApplication() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val customDialog = build.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
          }
        }
        true
      }
    }
  }

  @SuppressLint("StringFormatMatches")
  private fun initializeLayout() {
    // adapter initialize
    search = false
    MusicListMA = getAllAudio()
    musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
    binding.musicRV.apply {
      setHasFixedSize(true)
      setItemViewCacheSize(13)
      adapter = musicAdapter
    }
    binding.totalSongs.text = getString(R.string.total_songs_count, musicAdapter.itemCount)
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun requestRuntimePermissions(): Boolean {
    val permissionsToRequest = mutableListOf<String>()
    if (ActivityCompat.checkSelfPermission(
        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED) {
      permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED) {
      permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    if (permissionsToRequest.isNotEmpty()) {
      ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 13)
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
      var allPermissionsGranted = true
      for (result in grantResults) {
        if (result != PackageManager.PERMISSION_GRANTED) {
          allPermissionsGranted = false
          break
        }
      }
      if (allPermissionsGranted) {
        Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
        initializeLayout()
      } else {
        Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
        // Optionally handle re-requesting or showing an explanation to the user
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

  override fun onDestroy() {
    super.onDestroy()
    if (!PlayerActivity.isSongPlaying && PlayerActivity.musicService != null) {
      exitApplication()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.search_view_menu, menu)
    val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
    searchView.setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean = true

          override fun onQueryTextChange(newText: String?): Boolean {
            musicListSearch = ArrayList()
            if (newText != null) {
              val userInput = newText.lowercase()
              for (song in MusicListMA) {
                if (song.title.lowercase().contains(userInput)) musicListSearch.add(song)
                search = true
                musicAdapter.updateMusicList(musicListSearch)
              }
            }
            return true
          }
        })

    return super.onCreateOptionsMenu(menu)
  }
}
