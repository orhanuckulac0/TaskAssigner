package com.example.taskassigner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ItemBoardMemberBinding
import com.example.taskassigner.models.User

data class BoardMemberListItemsAdapter(val context: Context,
                                       val list: ArrayList<User>,
                                       val createdByID: String,
                                       val currentUserID: String)
    : RecyclerView.Adapter<BoardMemberListItemsAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    class ViewHolder(binding: ItemBoardMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivBoardMemberImage = binding.ivBoardMemberImage
        val tvBoardMemberName = binding.tvBoardMemberName
        val tvBoardMemberEmail = binding.tvBoardMemberEmail
        val ivBoardMemberDelete = binding.ivBoardMemberDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.ivBoardMemberImage)

        holder.tvBoardMemberName.text = model.name
        holder.tvBoardMemberEmail.text = model.email

        if (currentUserID == createdByID){
            holder.ivBoardMemberDelete.visibility = View.VISIBLE
        }

        if (model.id == createdByID){
            holder.ivBoardMemberDelete.visibility = View.GONE
        }

        // set each holder item except creator a setOnClickListener
        if (model.id != createdByID){
            holder.ivBoardMemberDelete.setOnClickListener {
                if (onItemClickListener != null){
                    onItemClickListener!!.onClick(model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnItemClickListener){
        this.onItemClickListener = onClickListener
    }

    interface OnItemClickListener {
        fun onClick(user: User)
    }
}