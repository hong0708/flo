package com.example.bottomnavitemplate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavitemplate.databinding.ItemLockerAlbumBinding

class AlbumLockerRVAdapter() : RecyclerView.Adapter<AlbumLockerRVAdapter.ViewHolder>() {
    private val albums = ArrayList<Album>()

    interface MyItemClickListener {
        fun onRemoveSong(songId: Int)
    }

    private lateinit var mItemClickListener: MyItemClickListener

    fun setMyItemClickListener(itemClickListener: MyItemClickListener){
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumLockerRVAdapter.ViewHolder {
        val binding: ItemLockerAlbumBinding =
            ItemLockerAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumLockerRVAdapter.ViewHolder, position: Int) {
        holder.bind(albums[position])
        holder.binding.ivSongMore.setOnClickListener {
            removeSong(position)
            mItemClickListener.onRemoveSong(albums[position].id)
        }
    }

    override fun getItemCount(): Int = albums.size

    @SuppressLint("notifyDataSetChanged")
    fun addAlbums(albums:ArrayList<Album>){
        this.albums.clear()
        this.albums.addAll(albums)
        notifyDataSetChanged()
    }
    fun removeSong(position: Int){
        albums.removeAt(position)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemLockerAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.ivAlbum.setImageResource(album.coverImg!!)
            binding.tvAlbumTitle.text = album.title
            binding.tvAlbumSinger.text = album.singer
        }
    }
}