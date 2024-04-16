package com.maruf.mmusicplayer.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.maruf.mmusicplayer.PlayerActivity
import com.maruf.mmusicplayer.R
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.MusicViewBinding
import com.maruf.mmusicplayer.util.Utils.formatDuration

class MusicAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<MusicAdapter.MyHolder>() {

  class MyHolder(val binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
    val binding = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
    return MyHolder(binding)
  }

  override fun onBindViewHolder(holder: MyHolder, position: Int) {
    holder.binding.apply {
      songNameMV.text = musicList[position].title
      songAlbumMV.text = musicList[position].album
      songDuration.text = formatDuration(musicList[position].duration)
      Log.d("TAG", "onBindViewHolder:${musicList[position].artUri}")
      /*Glide.with(context).load(musicList[position].artUri).into(imageMV)*/
      imageMV.load(musicList[position].artUri) {
        crossfade(true)
        placeholder(R.mipmap.ic_music_player_icon)
        error(R.mipmap.ic_music_player_icon)
        transformations(CircleCropTransformation())
      }
      root.setOnClickListener {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index",position)
        intent.putExtra("class","MusicAdapter")
        ContextCompat.startActivity(context, intent, null)
      }
    }
  }

  override fun getItemCount(): Int {
    return musicList.size
  }
}
