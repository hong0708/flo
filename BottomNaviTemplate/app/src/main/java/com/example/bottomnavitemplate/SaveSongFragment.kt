package com.example.bottomnavitemplate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavitemplate.databinding.FragmentSaveSongBinding

class SaveSongFragment : Fragment() {
    lateinit var binding: FragmentSaveSongBinding
    lateinit var songDB: SongDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveSongBinding.inflate(inflater, container, false)

        songDB = SongDatabase.getInstance(requireContext())!!

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initRecyclerview()
    }

    private fun initRecyclerview() {
        binding.songAlbumRecyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val songSongRVAdapter = SaveSongRVAdapter()
        //리스너 객체 생성 및 전달

        songSongRVAdapter.setSaveSongClickListener(object :
            SaveSongRVAdapter.SaveSongClickListener {

            override fun onRemoveClick(position: Int) {
                songDB.songDao().updateIsLikeById(false, position)
            }
        })

        binding.songAlbumRecyclerview.adapter = songSongRVAdapter

        songSongRVAdapter.addSongs(songDB.songDao().getLikedSongs(true) as ArrayList)

    }
}