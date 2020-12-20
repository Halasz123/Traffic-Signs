package com.example.trafficsigns.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.ui.fragments.List.SampleListFragment

class TrafficCollectionListAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var collectionList = emptyList<TrafficSignsCollection>()

    override fun getItemCount(): Int = collectionList.size

    override fun createFragment(position: Int): Fragment {
        return SampleListFragment.newInstance(collectionList[position].trafficSigns)
    }

    fun setData(collection: List<TrafficSignsCollection>){
        this.collectionList = collection
        notifyDataSetChanged()
    }
}