package com.example.taskassigner.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.databinding.ItemLabelColorBinding

data class LabelColorListItemsAdapter(private val context: Context,
                                      private var list: ArrayList<String>,
                                      private val mSelectedColor: String)
    : RecyclerView.Adapter<LabelColorListItemsAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    class ViewHolder(binding: ItemLabelColorBinding): RecyclerView.ViewHolder(binding.root){
        val ivSelectedColor = binding.ivSelectedColor
        val viewMainCardsDetailActivity = binding.viewMainCardsDetailActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLabelColorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // item passed here is a color string, like #FFFFFF
        val item = list[position]

        // mSelectedColor will be set when user selects a color,
        // otherwise It's ""
        holder.viewMainCardsDetailActivity.setBackgroundColor(Color.parseColor(item))
        if (item == mSelectedColor){
            // ivSelectedColor is a vector I created which is a tick sign
            holder.ivSelectedColor.visibility = View.VISIBLE
        }else{
            holder.ivSelectedColor.visibility = View.GONE
        }

        // setup onClickListener for each rv item
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null){
                onItemClickListener!!.onClick(position, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}