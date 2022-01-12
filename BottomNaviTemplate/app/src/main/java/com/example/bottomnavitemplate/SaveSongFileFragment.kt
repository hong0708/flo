package com.example.bottomnavitemplate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bottomnavitemplate.databinding.FragmentSaveSongFileBinding

class SaveSongFileFragment : Fragment(){
    lateinit var binding: FragmentSaveSongFileBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSaveSongFileBinding.inflate(inflater, container, false)
        return binding.root
    }
}