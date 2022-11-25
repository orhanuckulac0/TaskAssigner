package com.example.taskassigner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ItemMemberBinding
import com.example.taskassigner.models.User

data class MemberListItemsAdapter(val context: Context,
                                  val list: ArrayList<User> )
    : RecyclerView.Adapter<MemberListItemsAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivMemberImage = binding.ivMemberImage
        val tvMemberName = binding.tvMemberName
        val tvMemberEmail = binding.tvMemberEmail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.ivMemberImage)

        holder.tvMemberName.text = model.name
        holder.tvMemberEmail.text = model.email
    }

    override fun getItemCount(): Int {
        return list.size
    }
}