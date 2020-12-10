package com.example.trafficsigns.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trafficsigns.R
import com.example.trafficsigns.data.TrafficSign
import com.example.trafficsigns.data.TrafficSignsCollection
import com.example.trafficsigns.ui.fragments.Detail.DetailFragment
import com.example.trafficsigns.ui.interfaces.ItemClickListener
import kotlinx.android.synthetic.main.sample_list_item.view.*

class SampleListAdapter(private val itemClickListener: ItemClickListener): RecyclerView.Adapter<SampleListAdapter.MyViewHolder>(), Filterable {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private var mTrafficList = emptyList<TrafficSign>()
    private var mTrafficListAll = emptyList<TrafficSign>()

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

        Glide.with(holder.itemView).load(currentItem.image).override(holder.itemView.width,holder.itemView.height).into(holder.itemView.sign_imageView)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClickListener(currentItem)
        }

        holder.itemView.setOnLongClickListener {
            itemClickListener.onItemLongClickListener(currentItem)
            true
        }

    }

    fun setData(collection: List<TrafficSign>){
        this.mTrafficList = collection
        this.mTrafficListAll = mTrafficList
        notifyDataSetChanged()
    }

    private val filter = (object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<TrafficSign>()

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(mTrafficListAll)
            }
            else {
                mTrafficListAll.forEach {
                    if (it.name?.toLowerCase()?.contains(constraint.toString().toLowerCase()) == true) {
                        filteredList.add(it)
                    }
                }
            }
            val filterResult = FilterResults()
            filterResult.values = filteredList

            return filterResult
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mTrafficList = emptyList()
            mTrafficList = results?.values as List<TrafficSign>
            notifyDataSetChanged()
        }

    })

    override fun getFilter(): Filter {
        return filter
    }

}