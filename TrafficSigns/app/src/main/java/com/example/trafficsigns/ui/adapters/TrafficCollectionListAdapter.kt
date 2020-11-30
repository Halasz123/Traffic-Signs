package com.example.trafficsigns.ui.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.ui.fragments.List.ARG_OBJECT
import com.example.trafficsigns.ui.fragments.List.SampleListFragment

class TrafficCollectionListAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var collectionList = emptyList<TrafficSignsCollection>()
    private var fragmentType = false

    override fun getItemCount(): Int = collectionList.size

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return when(fragmentType) {
            false -> SampleListFragment.newInstance(collectionList[position].trafficSigns)
            else -> SampleListFragment()
        }
    }

    fun setData(collection: List<TrafficSignsCollection>){
        this.collectionList = collection
        notifyDataSetChanged()
    }

    fun setFragmentType(fragmentType: Boolean) {
        this.fragmentType = fragmentType
    }

}