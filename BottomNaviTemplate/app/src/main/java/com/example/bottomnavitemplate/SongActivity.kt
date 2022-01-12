package com.example.bottomnavitemplate

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bottomnavitemplate.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {

    lateinit var binding: ActivitySongBinding

    private lateinit var player: Player
    //private val handler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null

    private var songs = ArrayList<Song>()
    private var nowPos = 0
    private lateinit var songDB: SongDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()
        initClickListener()
    }

    override fun onPause() {
        super.onPause()

        songs[nowPos].second = (songs[nowPos].playTime * binding.songSeekbar.progress) / 1000
        songs[nowPos].isPlaying = false
        setPlayerStatus(false)

        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("songId", songs[nowPos].id)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        player.interrupt() // 스레드를 해제함
        mediaPlayer?.release() // 미디어플레이어가 가지고 있던 리소스를 해방
        mediaPlayer = null // 미디어플레이어 해제
    }

    private fun initPlayList() {
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }

    private fun startPlayer () {
        player = Player(songs[nowPos].playTime, songs[nowPos].isPlaying)
        player.start()
    }

    private fun initSong() {
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId", 0)

        nowPos = getPlayingSongPosition(songId)

        Log.d("now Song ID", songs[nowPos].id.toString())

        startPlayer()
        setPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId: Int): Int {
        for (i in 0 until songs.size) {
            if (songs[i].id == songId) {
                return i
            }
        }
        return 0
    }


    private fun setPlayer(song: Song) {
        val music = resources.getIdentifier(song.music, "raw", this.packageName)

        binding.songTitleTv.text = song.title
        binding.songSingerTv.text = song.singer
        binding.songSeekbarStartTv.text =
            String.format("%02d:%02d", song.second / 60, song.second % 60)
        binding.songSeekbarEndTv.text =
            String.format("%02d:%02d", song.playTime / 60, song.playTime % 60)
        binding.songCoverIv.setImageResource(song.coverImg!!)
        binding.songSeekbar.progress = (song.second * 1000 / song.playTime)

        setPlayerStatus(song.isPlaying)

        if (song.isLike) {
            binding.likeIv.setImageResource(R.drawable.ic_my_like_on)
        } else {
            binding.likeIv.setImageResource(R.drawable.ic_my_like_off)
        }

        mediaPlayer = MediaPlayer.create(this, music)
    }


    private fun initClickListener() {
        binding.songDownIb.setOnClickListener {
            finish()
        }

        binding.songMiniplayerIv.setOnClickListener {
            setPlayerStatus(true)
        }

        binding.songMiniplayerPauseIv.setOnClickListener {
            setPlayerStatus(false)
        }

        binding.songPreviousIv.setOnClickListener {
            moveSong(-1)
        }

        binding.songNextIv.setOnClickListener {
            moveSong(+1)
        }

        binding.likeIv.setOnClickListener {
            setLike(songs[nowPos].isLike)
        }
    }

    private fun moveSong(direct: Int) {

        if (nowPos + direct < 0) {
            Toast.makeText(this, "first song", Toast.LENGTH_SHORT).show()
            return
        }
        if (nowPos + direct >= songs.size) {
            Toast.makeText(this, "last song", Toast.LENGTH_SHORT).show()
            return
        }

        nowPos += direct

        player.interrupt()
        startPlayer()

        mediaPlayer?.release() // 미디어플레이어가 가지고 있던 리소스를 해방
        mediaPlayer = null // 미디어플레이어 해제

        setPlayer(songs[nowPos])
    }

    private fun setLike(isLike: Boolean) {
        songs[nowPos].isLike = !isLike
        songDB.songDao().updateIsLikeById(!isLike, songs[nowPos].id)

        if (isLike) {
            binding.likeIv.setImageResource(R.drawable.ic_my_like_off)
        } else {
            binding.likeIv.setImageResource(R.drawable.ic_my_like_on)
        }
    }

    fun setRandomStayus(isRandom: Boolean) {
        if (isRandom) {
            binding.songRandomInactiveIv.visibility = View.GONE
            binding.songRandomOnIv.visibility = View.VISIBLE
        } else {
            binding.songRandomInactiveIv.visibility = View.VISIBLE
            binding.songRandomOnIv.visibility = View.GONE
        }
    }

    fun setPlayerStatus(isPlaying: Boolean) {

        player.isPlaying = isPlaying
        songs[nowPos].isPlaying = isPlaying

        if (isPlaying) {
            binding.songMiniplayerIv.visibility = View.GONE
            binding.songMiniplayerPauseIv.visibility = View.VISIBLE
            mediaPlayer?.start()
        } else {
            binding.songMiniplayerIv.visibility = View.VISIBLE
            binding.songMiniplayerPauseIv.visibility = View.GONE
            mediaPlayer?.pause()
        }
    }

    fun setRepeatStatus(howRepeat: Int) {
        when (howRepeat) {
            1 -> {
                binding.songRepeatInactiveIv.visibility = View.GONE
                binding.songRepeatOnIv.visibility = View.VISIBLE
            }
            2 -> {
                binding.songRepeatOnIv.visibility = View.GONE
                binding.songRepeatOn1Iv.visibility = View.VISIBLE
            }
            3 -> {
                binding.songRepeatOn1Iv.visibility = View.GONE
                binding.songRepeatPlaylistIv.visibility = View.VISIBLE
            }
            4 -> {
                binding.songRepeatPlaylistIv.visibility = View.GONE
                binding.songRepeatInactiveIv.visibility = View.VISIBLE
            }
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
                            binding.songSeekbarStartTv.text =
                                String.format("%02d:%02d", second / 60, second % 60)
                            binding.songSeekbar.progress = second * 1000 / playTime
                        }
                    }
                }
            } catch (e: InterruptedException) {
                Log.d("interrupt", "쓰레드 종료")
            }
        }
    }

}