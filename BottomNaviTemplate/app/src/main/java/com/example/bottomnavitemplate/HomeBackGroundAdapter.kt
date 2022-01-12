package com.example.bottomnavitemplate

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeBackGroundAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentHomeList: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int = fragmentHomeList.size

    override fun createFragment(position: Int): Fragment = fragmentHomeList[position]

    fun addFragment(fragment: Fragment) {
        fragmentHomeList.add(fragment)
        notifyItemInserted(fragmentHomeList.size - 1)
    }
}