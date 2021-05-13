
package com.trafficsigns.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trafficsigns.R
import com.trafficsigns.data.dataclass.TrafficSignsCollection
import com.trafficsigns.ui.constant.General
import com.trafficsigns.ui.fragment.list.CollectionListFragment
import kotlinx.android.synthetic.main.group_item.view.*

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Set elements to the recyclerview on the main screen.
 */
class MainMenuAdapter(val context: Context?) : RecyclerView.Adapter<MainMenuAdapter.MyViewHolder>(){

    private var collectionList =  emptyList<TrafficSignsCollection>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val  currentItem = collectionList[position]

        holder.itemView.text_view_list_item.text = currentItem.groupId.capitalize() + General.SIGNS
        loadImageWithGlide(currentItem.trafficSigns[0].image, holder.itemView.image_view_list_item)
        loadImageWithGlide(currentItem.trafficSigns[1].image, holder.itemView.imageView)

        holder.itemView.setOnClickListener {
            it.findNavController().navigate(R.id.action_mainScreenFragment_to_collectionListFragment,
                CollectionListFragment.newInstanceBundle(position, collectionList))
        }
    }

    override fun getItemCount() = collectionList.count()

    fun changeData(collection: List<TrafficSignsCollection>){
        this.collectionList = collection
        notifyDataSetChanged()
    }

    private fun loadImageWithGlide(url: String?, imageView: ImageView) {
        context?.let {
            Glide
                .with(it)
                .load(url)
                .override(imageView.width, imageView.height)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView)
        }
    }
}