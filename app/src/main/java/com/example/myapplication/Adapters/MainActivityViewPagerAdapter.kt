package com.example.myapplication.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlin.properties.Delegates

class MainActivityViewPagerAdapter: FragmentPagerAdapter {
    private var fm: FragmentManager
    private var countOfFragments by Delegates.notNull<Int>()
    private var fragmentList: ArrayList<Pair<Fragment,String>>

    constructor(fm: FragmentManager, countOfFragments: Int) : super(fm) {
        this.fm = fm
        this.countOfFragments = countOfFragments
        fragmentList = ArrayList()
    }

    override fun getCount(): Int = this.countOfFragments

    override fun getItem(position: Int): Fragment {
        return fragmentList[position].first
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentList[position].second
    }

    fun addFragment(fragment: Fragment, title: String) {
        val fragmentPair: Pair<Fragment, String> = Pair(fragment, title)
        this.fragmentList.add(fragmentPair)
    }
}