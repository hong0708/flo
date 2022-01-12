package com.example.bottomnavitemplate

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bottomnavitemplate.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private var albumDatas = ArrayList<Album>()

    lateinit var mContext : Context

    private lateinit var songDB: SongDatabase

    val handler = Handler(Looper.getMainLooper()){
        setHomeBackGround()
        true
    }
    var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        //ROOM_DB
        songDB = SongDatabase.getInstance(requireContext())!!
        albumDatas.addAll(songDB.albumDao().getAlbums()) // songDB에서 album list를 가져옵니다.


        val albumRVAdapter = AlbumRVAdapter(albumDatas,mContext)
        binding.homeTodayMusicAlbumRecyclerview.adapter = albumRVAdapter
        albumRVAdapter.setMyItemClickListener(object : AlbumRVAdapter.MyItemClickListener{

            override fun onItemClick(album: Album) {
                changeAlbumFragment(album)
            }

            override fun onPlayClick(album: Album) {
                val mActivity = activity as MainActivity
                mActivity.receiveAlbum(album)
            }
        })
        binding.homeTodayMusicAlbumRecyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)


        val bannerAdapter = BannerViewpagerAdapter(this)
        bannerAdapter.addFragment(BannerFragment(R.drawable.img_home_viewpager_exp))
        bannerAdapter.addFragment(BannerFragment(R.drawable.img_home_viewpager_exp2))

        binding.homeViewpager.adapter = bannerAdapter
        binding.homeViewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val homeAdapter = HomeBackGroundAdapter(this)
        homeAdapter.addFragment(BackGroundFragment(R.drawable.img_default_4_x_1))
        homeAdapter.addFragment(BackGroundFragment(R.drawable.img_default_4_x_1))
        homeAdapter.addFragment(BackGroundFragment(R.drawable.img_default_4_x_1))

        binding.homeBackgroundVp.adapter = homeAdapter
        binding.homeBackgroundVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.homeBackgroundTb, binding.homeBackgroundVp) { tab, position ->

        }.attach()

        val mainBackGroundThread = Thread(setHomeBackGroundRunnable())
        mainBackGroundThread.start()
        return binding.root
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun changeAlbumFragment(album: Album) {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, AlbumFragment().apply {
                arguments = Bundle().apply {
                    val gson = Gson()
                    val albumJson = gson.toJson(album)
                    putString("album", albumJson)
                }
            })
            .commitAllowingStateLoss()
    }

    private fun setHomeBackGround() {
        if (currentPage == 3) currentPage = 0
        binding.homeBackgroundVp.setCurrentItem(currentPage,true)
        currentPage += 1
    }

    inner class setHomeBackGroundRunnable : Runnable{
        override fun run() {
            while (true) {
                Thread.sleep(2000)
                handler.sendEmptyMessage(0)
            }
        }
    }
}