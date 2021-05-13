package com.trafficsigns.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.trafficsigns.data.dataclass.TrafficSignsCollection
import com.trafficsigns.ui.fragment.list.SampleListFragment

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Adapter of the view pager on the CollectionListFragment.
 * Set SampleList fragments to the view pager.
 */
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