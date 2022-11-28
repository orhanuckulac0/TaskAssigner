package com.example.taskassigner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ItemCardSelectedMemberBinding
import com.example.taskassigner.models.SelectedMembers

open class CardMemberListItemsAdapter(val context: Context,
                                      val list: ArrayList<SelectedMembers> )
    : RecyclerView.Adapter<CardMemberListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnCLickListener? = null

    class ViewHolder(binding: ItemCardSelectedMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivSelectedMemberImage = binding.ivSelectedMemberImage
        val ivAddMember = binding.ivAddMember
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        // get list.sie -1 because + button to add members is also in the view
        if (position == list.size - 1){
            holder.ivAddMember.visibility = View.VISIBLE
            holder.ivSelectedMemberImage.visibility = View.GONE
        }else{
            holder.ivAddMember.visibility = View.GONE
            holder.ivSelectedMemberImage.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.ivSelectedMemberImage)
        }
        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnCLickListener){
        this.onClickListener = onClickListener
    }

    interface OnCLickListener {
        fun onClick()
    }
}