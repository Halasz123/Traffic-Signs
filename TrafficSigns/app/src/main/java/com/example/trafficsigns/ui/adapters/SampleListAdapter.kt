package com.example.trafficsigns.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.ui.fragments.Detail.DetailFragment
import kotlinx.android.synthetic.main.sample_list_item.view.*

class SampleListAdapter(trafficList: List<TrafficSign>, parentFragmentManager: FragmentManager): RecyclerView.Adapter<SampleListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    private val mTrafficList = trafficList
    private val parentFragmentManager = parentFragmentManager

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.sample_list_item,
            parent,
            false
        )
        return MyViewHolder(itemView)
        }

    override fun getItemCount() = mTrafficList.count()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val  currentItem = mTrafficList[position]

        holder.itemView.name_textView.text = currentItem.name

        Glide.with(holder.itemView).load(currentItem.image).override(holder.itemView.width,holder.itemView.height).into(holder.itemView.sign_imageView);

        holder.itemView.setOnClickListener {
            val myFragment = DetailFragment.newInstance()
            myFragment.let { it1 ->
                parentFragmentManager.beginTransaction().replace(
                        R.id.main_framelayout,
                        it1
                ).addToBackStack(null).commit()
            }
        }

    }
}