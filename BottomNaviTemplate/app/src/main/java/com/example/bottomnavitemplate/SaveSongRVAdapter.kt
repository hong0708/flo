package com.example.bottomnavitemplate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavitemplate.databinding.ItemSongBinding

class SaveSongRVAdapter() :
    RecyclerView.Adapter<SaveSongRVAdapter.ViewHolder>() {
    private val songs = ArrayList<Song>()

    interface SaveSongClickListener {
        fun onRemoveClick(position: Int)
    }

    private lateinit var saveSongClickListener: SaveSongClickListener

    fun setSaveSongClickListener(itemCLickListener: SaveSongClickListener) {
        saveSongClickListener = itemCLickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SaveSongRVAdapter.ViewHolder {
        val binding: ItemSongBinding =
            ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaveSongRVAdapter.ViewHolder, position: Int) {
        holder.bind(songs[position])
        holder.binding.deleteIv.setOnClickListener {
            removeSong(position)
            saveSongClickListener.onRemoveClick(songs[position].id) }
    }

    override fun getItemCount(): Int = songs.size

    @SuppressLint("NotifyDataSetChanged")
    fun addSongs(songs: ArrayList<Song>) {
        this.songs.clear()
        this.songs.addAll(songs)

        notifyDataSetChanged()
    }

    fun removeSong(position: Int){
        songs.removeAt(position)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.iv1FLOChart.setImageResource(song.coverImg!!)
            binding.tvFLOChartTitle.text = song.title
            binding.tvFLOChartSinger.text = song.singer
        }
    }
}