package com.maruf.mmusicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.ActivityPlayerBinding
import com.maruf.mmusicplayer.service.MusicService
import com.maruf.mmusicplayer.util.Utils
import com.maruf.mmusicplayer.util.Utils.exitApplication
import java.util.Timer
import java.util.TimerTask

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

  @SuppressLint("StaticFieldLeak")
  companion object {
    lateinit var musicListPA: ArrayList<Music>
    var songPosition = 0
    var isSongPlaying: Boolean = false
    var musicService: MusicService? = null
    lateinit var binding: ActivityPlayerBinding
    var repeat: Boolean = false
    var min15: Boolean = false
    var min30: Boolean = false
    var min60: Boolean = false
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
      // back button
      backBtnPA.setOnClickListener { finish() }
      // play pause button
      playPauseBtnPA.setOnClickListener { if (isSongPlaying) pauseMusic() else playMusic() }
      // previous button
      previousBtnPA.setOnClickListener { nextOrPreviousSong(false) }
      // next button
      nextBtnPA.setOnClickListener { nextOrPreviousSong(true) }
      // seekBar
      seekBarPA.setOnSeekBarChangeListener(
          object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
              if (fromUser) musicService?.mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
          })
      // repeatBtn
      repeatBtnPA.setOnClickListener {
        if (!repeat) {
          repeat = true
          binding.repeatBtnPA.setColorFilter(
              ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
        } else {
          repeat = false
          binding.repeatBtnPA.setColorFilter(
              ContextCompat.getColor(this@PlayerActivity, R.color.cool_pink))
        }
      }
      // equalizer
      equalizerBtnPA.setOnClickListener {
        try {
          val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
          eqIntent.putExtra(
              AudioEffect.EXTRA_AUDIO_SESSION, musicService?.mediaPlayer?.audioSessionId)
          eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
          eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
          resultLauncher.launch(eqIntent)
        } catch (e: Exception) {
          Toast.makeText(
                  this@PlayerActivity, "Equalizer Feature not Supported!!", Toast.LENGTH_SHORT)
              .show()
        }
      }
      // timer button
      timerBtnPA.setOnClickListener {
        val timer = min15 || min30 || min60
        if (!timer) showBottomSheetDialog()
        else {
          val build = MaterialAlertDialogBuilder(this@PlayerActivity)
          build
              .setTitle("Stop Timer")
              .setMessage("Do you want to stop timer?")
              .setPositiveButton("Yes") { _, _ ->
                stopTimer()
              }
              .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
          val customDialog = build.create()
          customDialog.show()
          customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
          customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
      }
      //share button
      shareBtnPA.setOnClickListener {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
        startActivity(Intent.createChooser(shareIntent,"Sharing Musing File!!"))
      }
    }
  }
  private fun stopTimer() {
    min15 = false
    min30 = false
    min60 = false
    binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this@PlayerActivity, R.color.cool_pink))
  }
  private fun initializeLayout() {
    songPosition = intent.getIntExtra("index", 0)
    when (intent.getStringExtra("class")) {
      "MusicAdapterSearch"->{
        musicListPA= ArrayList()
        musicListPA.addAll(MainActivity.musicListSearch)
        setLayout()
      }
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
    if (repeat)
        binding.repeatBtnPA.setColorFilter(
            ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
    if (min15 || min30 || min60) {
      binding.timerBtnPA.setColorFilter(
          ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
    }
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
        musicService!!.showNotification(R.drawable.pause_icon)
        binding.tvSeekBarStart.text =
            musicService?.mediaPlayer?.currentPosition?.toLong()?.let { Utils.formatDuration(it) }
        binding.tvSeekBarEnd.text =
            musicService?.mediaPlayer?.duration?.toLong()?.let { Utils.formatDuration(it) }
        binding.seekBarPA.apply {
          progress = 0
          max = musicService?.mediaPlayer?.duration!!
        }
        musicService?.mediaPlayer?.setOnCompletionListener(this@PlayerActivity)
      }
    } catch (e: Exception) {
      return
    }
  }

  private fun playMusic() {
    binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
    musicService?.showNotification(R.drawable.pause_icon)
    isSongPlaying = true
    musicService?.mediaPlayer?.start()
  }

  private fun pauseMusic() {
    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    musicService?.showNotification(R.drawable.play_icon)
    isSongPlaying = false
    musicService?.mediaPlayer?.pause()
  }

  private fun nextOrPreviousSong(increment: Boolean) {
    Utils.setSongPosition(increment)
    setLayout()
    createMediaPlayer()
  }

  override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
    val binder = service as MusicService.MyBinder
    musicService = binder.currentService()
    createMediaPlayer()
    musicService?.seekBarSetup()
  }

  override fun onServiceDisconnected(p0: ComponentName?) {
    musicService = null
  }

  override fun onCompletion(p0: MediaPlayer?) {
    Utils.setSongPosition(true)
    createMediaPlayer()
    try {
      setLayout()
    } catch (e: Exception) {
      return
    }
  }

  // for activity result
  private var resultLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) return@registerForActivityResult
      }

  private fun showBottomSheetDialog() {
    val dialog = BottomSheetDialog(this@PlayerActivity)
    dialog.setContentView(R.layout.bottom_sheet_dialog)
    val timerButtons = listOf(R.id.min_15, R.id.min_30, R.id.min_60)

    timerButtons.forEach { buttonId ->
      dialog.findViewById<LinearLayout>(buttonId)?.setOnClickListener {
        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
        val selectedTime = when (buttonId) {
          R.id.min_15 -> 15  // Set time in minutes based on button ID
          R.id.min_30 -> 30
          R.id.min_60 -> 60
          else -> 0  // Handle potential unexpected button ID
        }
        startTimer(selectedTime)  // Call a separate function to handle timer logic
        dialog.dismiss()
      }
    }

    dialog.show()

   /* val dialog = BottomSheetDialog(this@PlayerActivity)
    dialog.setContentView(R.layout.bottom_sheet_dialog)
    dialog.show()
    dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
      binding.timerBtnPA.setColorFilter(
          ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
      min15 = true
      Thread {
            Thread.sleep(15 * 60000)
            if (min15) exitApplication()
          }
          .start()
      dialog.dismiss()
    }
    dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
      binding.timerBtnPA.setColorFilter(
          ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
      min30 = true
      Thread {
            Thread.sleep(30 * 60000)
            if (min30) exitApplication()
          }
          .start()
      dialog.dismiss()
    }
    dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
      binding.timerBtnPA.setColorFilter(
          ContextCompat.getColor(this@PlayerActivity, R.color.purple_500))
      min60 = true
      Thread {
            Thread.sleep(60 * 60000)
            if (min60) exitApplication()
          }
          .start()
      dialog.dismiss()
    }*/
  }

  private fun startTimer(minutes: Int) {
    Timer().schedule(object : TimerTask() {
      override fun run() {
        runOnUiThread {  // Update UI elements from within the timer thread
            exitApplication()
        }
      }
    }, minutes * 60 * 1000L)  // Convert minutes to milliseconds
  }
}
