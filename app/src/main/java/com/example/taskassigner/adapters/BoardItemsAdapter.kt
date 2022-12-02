package com.example.taskassigner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ItemBoardBinding
import com.example.taskassigner.models.Board


open class BoardItemsAdapter(private val context: Context,
                             private val list: ArrayList<Board>):
    RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root){
        val tvBoardName = binding.tvBoardName
        val tvCreatedBy = binding.tvCreatedBy
        val ivBoardItemImage = binding.ivBoardItemImage
        val boardLabelColor = binding.boardLabelColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        Glide
            .with(context)
            .load(model.image)  // load requires Uri
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(holder.ivBoardItemImage)

//        holder.boardLabelColor.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        holder.tvBoardName.text = model.name
        holder.tvCreatedBy.text = "Created by: ${model.createdBy}" // for now

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick(position, model)
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }
}