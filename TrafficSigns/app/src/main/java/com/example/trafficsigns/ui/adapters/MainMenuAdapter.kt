package com.example.trafficsigns.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.MainActivity
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.ui.fragments.List.CollectionListFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.group_item.view.*


class MainMenuAdapter(parent: FragmentManager) : RecyclerView.Adapter<MainMenuAdapter.MyViewHolder>(){

    private var collectionList =  emptyList<TrafficSignsCollection>()
    private val mFragmentManager = parent


    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainMenuAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.group_item,
            parent,
            false
        )
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MainMenuAdapter.MyViewHolder, position: Int) {
        val  currentItem = collectionList[position]

        holder.itemView.text_view_list_item.text = currentItem.groupId

        holder.itemView.setOnClickListener {
            val myFragment = CollectionListFragment()
            mFragmentManager.beginTransaction().replace(R.id.main_framelayout, myFragment).addToBackStack(null).commit()
        }
    }

    override fun getItemCount() = collectionList.count()

    fun setData(collection: List<TrafficSignsCollection>){
        this.collectionList = collection
        notifyDataSetChanged()
    }
}