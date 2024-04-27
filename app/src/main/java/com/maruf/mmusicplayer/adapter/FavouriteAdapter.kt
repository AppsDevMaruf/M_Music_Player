package com.maruf.mmusicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.maruf.mmusicplayer.FavouriteActivity
import com.maruf.mmusicplayer.PlayerActivity
import com.maruf.mmusicplayer.R
import com.maruf.mmusicplayer.data.Music
import com.maruf.mmusicplayer.databinding.FavouriteViewBinding
import com.maruf.mmusicplayer.util.Utils.sendIntent

class FavouriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<FavouriteAdapter.MyHolder>() {

  class MyHolder(val binding: FavouriteViewBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
    val binding = FavouriteViewBinding.inflate(LayoutInflater.from(context), parent, false)
    return MyHolder(binding)
  }

  @SuppressLint("NotifyDataSetChanged")
  override fun onBindViewHolder(holder: MyHolder, position: Int) {
    holder.binding.apply {

      songNameFV.text = musicList[position].title
      songImgFV.load(musicList[position].artUri) {
        crossfade(true)
        placeholder(R.mipmap.ic_music_player_icon)
        error(R.mipmap.ic_music_player_icon)
        transformations(CircleCropTransformation())
      }
      root.setOnClickListener {
        sendIntent(context,"FavouriteAdapter",position)
      }
    }
  }

  override fun getItemCount(): Int {
    return musicList.size
  }


}
