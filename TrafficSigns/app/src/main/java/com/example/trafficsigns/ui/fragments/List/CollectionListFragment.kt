package com.example.trafficsigns.ui.fragments.List

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.trafficsigns.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CollectionListFragment : Fragment() {

    private lateinit var demoCollectionAdapter: TrafficCollectionListAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        demoCollectionAdapter = TrafficCollectionListAdapter(this)
        viewPager = view.findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = demoCollectionAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CollectionListFragment()
    }
}