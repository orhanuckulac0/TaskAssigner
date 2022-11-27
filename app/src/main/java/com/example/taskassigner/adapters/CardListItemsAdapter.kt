package com.example.taskassigner.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.databinding.ItemCardBinding
import com.example.taskassigner.models.Card

open class CardListItemsAdapter(private val context: Context,
                                private val list: ArrayList<Card>)
    : RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root){
        val tvCardName = binding.tvCardName
        val tvMembersName = binding.tvMembersName
        val viewLabelColor = binding.viewLabelColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        // checking for "" because by default its ""
        if (model.labelColor.isNotEmpty() && model.labelColor != ""){
            // viewLabelColor is showing the selected color by user on TaskListActivity, just above the card
            holder.viewLabelColor.visibility = View.VISIBLE
            holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.viewLabelColor.visibility = View.GONE
        }

        holder.tvCardName.text = model.name

        holder.itemView.setOnClickListener{
            if (onClickListener != null) {
                onClickListener!!.onClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }
}