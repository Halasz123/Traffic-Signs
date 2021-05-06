package com.trafficsigns.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trafficsigns.R
import com.trafficsigns.data.TrafficSign
import com.trafficsigns.ui.fragments.detail.DetailFragment
import com.trafficsigns.ui.interfaces.ItemLongClickListener
import com.trafficsigns.ui.utils.Settings
import kotlinx.android.synthetic.main.sample_list_item.view.*

class SampleListAdapter(private val onClickNavigateAction: Int, private val itemLongClickListener: ItemLongClickListener?, val context: Context?): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var mTrafficList = emptyList<TrafficSign>()
    private var mTrafficListAll = emptyList<TrafficSign>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): RecyclerView.ViewHolder {
        return when (Settings.isGrid) {
            true -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sample_grid_item, parent, false)
                GridViewHolder(itemView)
            }
            false -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sample_list_item, parent, false)
                ListViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = mTrafficList[position]
        if (!Settings.isGrid) {
            holder.itemView.name_textView.text = currentItem.name
        }
        context?.let { Glide.with(it).load(currentItem.image).override(holder.itemView.width, holder.itemView.height).into(holder.itemView.sign_imageView) }

        holder.itemView.setOnClickListener {
            it.findNavController().navigate(onClickNavigateAction, DetailFragment.newInstanceBundle(currentItem))
        }

        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClickListener(currentItem)
            true
        }
    }

    override fun getItemCount() = mTrafficList.count()

    fun changeData(collection: List<TrafficSign>) {
        this.mTrafficList = collection
        this.mTrafficListAll = mTrafficList
        notifyDataSetChanged()
    }

    private val filter = (object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<TrafficSign>()

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(mTrafficListAll)
            } else {
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

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            mTrafficList = emptyList()
            mTrafficList = results.values as List<TrafficSign>
            notifyDataSetChanged()
        }
    })

    override fun getFilter(): Filter {
        return filter
    }
}

