package com.example.trafficsigns.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSignsCollection
import kotlinx.android.synthetic.main.group_item.view.*

class MainMenuAdapter : RecyclerView.Adapter<MainMenuAdapter.MyViewHolder>(){

    private var collectionList =  emptyList<TrafficSignsCollection>()


    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainMenuAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MainMenuAdapter.MyViewHolder, position: Int) {
        val  currentItem = collectionList[position]

        holder.itemView.text_view_list_item.text = currentItem.groupId
    }

    override fun getItemCount() = collectionList.count()

    fun setData(collection: List<TrafficSignsCollection>){
        this.collectionList = collection
        notifyDataSetChanged()
    }
}