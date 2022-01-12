package com.example.bottomnavitemplate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bottomnavitemplate.databinding.FragmentSongBinding

class SongFragment : Fragment() {
    lateinit var binding: FragmentSongBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSongBinding.inflate(inflater, container, false)

        binding.mixOffIv.setOnClickListener { mixBtnStatus(true) }
        binding.mixOnIv.setOnClickListener { mixBtnStatus(false) }

        binding.songLalacLayout.setOnClickListener {
            Toast.makeText(activity,"라일락",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    fun mixBtnStatus(isMix:Boolean){
        if (isMix){
            binding.mixOffIv.visibility = View.GONE
            binding.mixOnIv.visibility = View.VISIBLE
        } else{

            binding.mixOffIv.visibility = View.VISIBLE
            binding.mixOnIv.visibility = View.GONE
        }
    }
}