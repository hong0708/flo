package com.example.bottomnavitemplate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bottomnavitemplate.databinding.FragmentAlbumBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson

class AlbumFragment : Fragment() {

    lateinit var binding: FragmentAlbumBinding
    private var gson: Gson = Gson()
    val information = arrayListOf("수록곡", "상세정보", "영상")

    private var isLiked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)

        //home data get
        val albumData = arguments?.getString("album")
        val album = gson.fromJson(albumData, Album::class.java)

        isLiked = isLikedAlbum(album.id)

        setInit(album)
        setClickListeners(album)

        val songs = getSongs(album.id)

        binding.backToHomeIv.setOnClickListener {
            val activity = activity as MainActivity
            activity.replaceFragment(HomeFragment())
        }

        val albumAdapter = AlbumViewpagerAdapter(this)
        binding.albumContentVp.adapter = albumAdapter

        TabLayoutMediator(binding.albumContentTb, binding.albumContentVp) { tab, position ->
            tab.text = information[position]
        }.attach()

        return binding.root
    }

    private fun setInit(album: Album) {
        binding.songCoverIv.setImageResource(album.coverImg!!)
        binding.albumTitleTv.text = album.title.toString()
        binding.albumSingerTv.text = album.singer.toString()

        if (isLiked) {
            binding.myLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
        } else {
            binding.myLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
        }
    }

    private fun setClickListeners(album:Album){
        val userId:Int = getUserIdx(requireContext())

        binding.myLikeOffIv.setOnClickListener {
            if (isLiked) {
                binding.myLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
                disLikedAlbum(userId,album.id)
            } else {
                binding.myLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
                likeAlbum(userId, album.id)
            }
        }
    }

    private fun likeAlbum(userId: Int, albumId: Int) {
        val songDB = SongDatabase.getInstance(requireContext())!!
        val like = Like(userId, albumId)

        songDB.albumDao().likeAlbum((like))
    }

    private fun isLikedAlbum(albumId: Int): Boolean {
        val songDB = SongDatabase.getInstance(requireContext())!!
        val userId = getUserIdx(requireContext())

        val likeId: Int? = songDB.albumDao().isLikeAlbum(userId, albumId)

        return likeId != null
    }

    private fun disLikedAlbum(userId: Int, albumId: Int) {
        val songDB = SongDatabase.getInstance(requireContext())!!
        songDB.albumDao().isLikeAlbum(userId, albumId)
    }

    private fun getJwt(): Int {
        val spf = activity?.getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        return spf!!.getInt("jwt", 0)
    }

    //ROOM_DB
    private fun getSongs(albumIdx: Int): ArrayList<Song> {
        val songDB = SongDatabase.getInstance(requireContext())!!

        val songs = songDB.songDao().getSongsInAlbum(albumIdx) as ArrayList

        return songs
    }
}