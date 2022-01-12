package com.example.bottomnavitemplate

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bottomnavitemplate.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var gson: Gson = Gson()
    private var song: Song = Song()

    lateinit var player: Player
    private var mediaPlayer: MediaPlayer? = null

    private var songList = arrayListOf<Song>()
    private var nowPos = 0
    private lateinit var songDB : SongDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initNavigation()
        inputDummyAlbums()
        inputDummySongs()

        binding.mainPlayerLayout.setOnClickListener {
            Log.d("nowSongId", song.id.toString())
            val editor = getSharedPreferences("song", MODE_PRIVATE).edit()
            editor.putInt("songId", song.id)
            editor.apply()

            val intent = Intent(this@MainActivity, SongActivity::class.java)
            // 좋아요가 업데이트 됐을 수도 있기 때문에
            // roomDB에서 songList에 있는 곡들을 다시 가져온다.
            var tmp_list = ArrayList<Song>()
            songList.forEach {
                tmp_list.add(songDB.songDao().getSong(it.id))
            }

            val songJson = gson.toJson(tmp_list)
            intent.putExtra("songs",songJson)

            startActivity(intent)
        }

        binding.mainMiniplayerBtn.setOnClickListener {
            setPlayerStatus(true)
        }

        binding.mainPauseBtn.setOnClickListener {
            setPlayerStatus(false)
        }

        binding.btnMiniplayerPrevious.setOnClickListener {
            playPrev()
        }

        binding.btnMiniplayerNext.setOnClickListener {
            playNext()
        }

        binding.mainProgressbarSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                song.second = (seekBar!!.progress * song.playTime) / 1000
                player.updateSecond(songList[nowPos].second)
                mediaPlayer?.seekTo(song.second * 1000)
            }

        })
    }

    override fun onStart() {
        super.onStart()
        initPlayList()

        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId", 0)

        nowPos = if(songId == 0)
            0
        else
            getPlayingSongPosition(songId)

        setMiniPlayer(songList[nowPos])
    }

    override fun onPause() {
        super.onPause()

        songList[nowPos].second = (binding.mainProgressbarSb.progress * songList[nowPos].playTime) / 100
        songList[nowPos].isPlaying = false
        setPlayerStatus(false)

        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("songId", songList[nowPos].id)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    private fun getPlayingSongPosition(songId : Int) : Int {
        for(i in 0 until songList.size) {
            if(songList[i].id == songId)
                return i
        }
        return 0
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .commitAllowingStateLoss()
    }

    // HomeFragment에 있는 앨범을 눌렀을 때 해당 앨범의 곡들을 songList에 추가
    fun receiveAlbum(album : Album) {
        songList.clear()
        player.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
        songList.addAll(songDB.songDao().getSongsInAlbum(album.id))
        //Log.d(TAG, "receiveAlbum: $songList")
        nowPos = 0
        setMiniPlayer(songList[0])
    }


    private fun initNavigation() {
        supportFragmentManager.beginTransaction().replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.lookFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, LookFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.searchFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, SearchFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.lockerFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, LockerFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

            }
            false
        }
    }

    private fun playNext() {
        songList[nowPos].isPlaying = false
        songList[nowPos].second = 0

        if(nowPos == songList.size-1)
            nowPos = 0
        else nowPos++

        player.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
        setMiniPlayer(songList[nowPos])
    }

    private fun playPrev() {
        songList[nowPos].isPlaying = false
        songList[nowPos].second = 0

        if(nowPos == 0)
            nowPos = songList.size - 1
        else nowPos--

        player.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
        setMiniPlayer(songList[nowPos])
    }

    private fun initPlayList() {
        songDB = SongDatabase.getInstance(this)!!
        songList.clear()

        // SongActivity로부터 돌아올 때
        if(intent.hasExtra("songs")) {
            songList = gson.fromJson(intent.getStringExtra("songs"), object : TypeToken<ArrayList<Song>>(){}.type)
            //Log.d(TAG, "initPlayList: $songList")
        }
        else {  // 처음 실행하는 경우
            songList.addAll(songDB.songDao().getSongs())
        }
    }

    inner class Player(private val playTime: Int, var isPlaying: Boolean) : Thread() {
        private var second = 0

        override fun run() {
            try {
                while (true) {
                    if (second >= playTime) break

                    if (isPlaying) {
                        sleep(1000)
                        second++
                        //다른 방법
                        /*runOnUiThread {
                        binding...
                    }*/
                        // 메인 쓰레드에게 이 뷰 렌더링하는 작업을 해달라는 메세지
                        runOnUiThread {
                            binding.mainProgressbarSb.progress = second * 1000 / playTime
                        }
                    }
                }
            } catch (e: InterruptedException) {
                Log.d("interrupt", "쓰레드 종료")
            }
        }
        fun stopPlayer() {
            song.isPlaying = false
        }

        fun playPlayer() {
            song.isPlaying = true
        }

        fun updateSecond(second : Int) {
            song.second = second
        }
    }

    //ROOM_DB
    private fun inputDummySongs() {
        val songDB = SongDatabase.getInstance(this)!!
        val songs = songDB.songDao().getSongs()

        if (songs.isNotEmpty()) return

        songDB.songDao().insert(
            Song(
                "Lilac",
                "아이유 (IU)",
                0,
                200,
                false,
                "music_lilac",
                R.drawable.img_album_exp2,
                false,
                1
            )
        )

        songDB.songDao().insert(
            Song(
                "Flu",
                "아이유 (IU)",
                0,
                200,
                false,
                "music_lilac",
                R.drawable.img_album_exp2,
                false,
                1
            )
        )

        songDB.songDao().insert(
            Song(
                "Butter",
                "방탄소년단 (BTS)",
                0,
                190,
                false,
                "music_lilac",
                R.drawable.img_album_exp,
                false,
                2
            )
        )

        songDB.songDao().insert(
            Song(
                "Butter (Hotter Remix)",
                "방탄소년단 (BTS)",
                0,
                190,
                false,
                "music_lilac",
                R.drawable.img_album_exp,
                false,
                2
            )
        )

        songDB.songDao().insert(
            Song(
                "Butter (Sweeter Remix)",
                "방탄소년단 (BTS)",
                0,
                190,
                false,
                "music_lilac",
                R.drawable.img_album_exp,
                false,
                2
            )
        )

        songDB.songDao().insert(
            Song(
                "Next Level",
                "에스파 (AESPA)",
                0,
                210,
                false,
                "music_lilac",
                R.drawable.img_album_exp3,
                false,
                3
            )
        )

        songDB.songDao().insert(
            Song(
                "Next Level (IMLAY Remix)",
                "에스파 (AESPA)",
                0,
                210,
                false,
                "music_lilac",
                R.drawable.img_album_exp3,
                false,
                3
            )
        )

        songDB.songDao().insert(
            Song(
                "Boy with Luv",
                "방탄소년단 (BTS)",
                0,
                230,
                false,
                "music_lilac",
                R.drawable.img_album_exp4,
                false,
                4
            )
        )

        songDB.songDao().insert(
            Song(
                "소우주 (Mikrokosmos)",
                "방탄소년단 (BTS)",
                0,
                230,
                false,
                "music_lilac",
                R.drawable.img_album_exp4,
                false,
                4
            )
        )

        songDB.songDao().insert(
            Song(
                "Make It Right",
                "방탄소년단 (BTS)",
                0,
                230,
                false,
                "music_lilac",
                R.drawable.img_album_exp4,
                false,
                4
            )
        )

        songDB.songDao().insert(
            Song(
                "BBoom BBoom",
                "모모랜드 (MOMOLAND)",
                0,
                240,
                false,
                "music_lilac",
                R.drawable.img_album_exp5,
                false,
                5
            )
        )

        songDB.songDao().insert(
            Song(
                "궁금해",
                "모모랜드 (MOMOLAND)",
                0,
                240,
                false,
                "music_lilac",
                R.drawable.img_album_exp5,
                false,
                5
            )
        )

        //디비 데이터 로그 찍기기
        val _songs = songDB.songDao().getSongs()
        Log.d("DB DATA", _songs.toString())
    }

    //ROOM_DB
    private fun inputDummyAlbums() {
        val songDB = SongDatabase.getInstance(this)!!
        val albums = songDB.albumDao().getAlbums()

        if (albums.isNotEmpty()) return

        songDB.albumDao().insert(
            Album(
                1,
                "IU 5th Album 'LILAC'", "아이유 (IU)", R.drawable.img_album_exp2
            )
        )

        songDB.albumDao().insert(
            Album(
                2,
                "Butter", "방탄소년단 (BTS)", R.drawable.img_album_exp
            )
        )

        songDB.albumDao().insert(
            Album(
                3,
                "iScreaM Vol.10 : Next Level Remixes", "에스파 (AESPA)", R.drawable.img_album_exp3
            )
        )

        songDB.albumDao().insert(
            Album(
                4,
                "MAP OF THE SOUL : PERSONA", "방탄소년단 (BTS)", R.drawable.img_album_exp4
            )
        )

        songDB.albumDao().insert(
            Album(
                5,
                "GREAT!", "모모랜드 (MOMOLAND)", R.drawable.img_album_exp5
            )
        )

    }


    fun setPlayerStatus(isPlaying: Boolean) {
        if (isPlaying) {
            player.playPlayer()
            mediaPlayer?.start()
            binding.mainMiniplayerBtn.visibility = View.GONE
            binding.mainPauseBtn.visibility = View.VISIBLE
        } else {
            player.stopPlayer()
            if (mediaPlayer?.isPlaying == true)
                mediaPlayer?.pause()
            binding.mainMiniplayerBtn.visibility = View.VISIBLE
            binding.mainPauseBtn.visibility = View.GONE
        }
    }

    private fun setMiniPlayer(song: Song) {
        binding.mainMiniplayerTitleTv.text = song.title
        binding.mainMiniplayerSingerTv.text = song.singer
        binding.mainProgressbarSb.progress = (song.second * 1000 / song.playTime)

        val music = resources.getIdentifier(song.music, "raw", this.packageName)

        mediaPlayer = MediaPlayer.create(this, music)
        mediaPlayer!!.seekTo(song.second * 1000)
        player = Player(song.playTime, song.isPlaying)
        player.start()

        setPlayerStatus(song.isPlaying)

        if (song.isPlaying) {
            binding.mainMiniplayerBtn.visibility = View.GONE
            binding.mainPauseBtn.visibility = View.VISIBLE
        } else {
            binding.mainMiniplayerBtn.visibility = View.VISIBLE
            binding.mainPauseBtn.visibility = View.GONE
        }
    }

}

