package com.example.taskassigner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.databinding.ItemCardBinding
import com.example.taskassigner.models.Card

open class CardListItemsAdapter(private val context: Context,
                                private val list: ArrayList<Card>)
    : RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>(){

    class ViewHolder(binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root){
        val tvCardName = binding.tvCardName
        val tvMembersName = binding.tvMembersName

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.tvCardName.text = model.name
    }

    override fun getItemCount(): Int {
        return list.size
    }
}