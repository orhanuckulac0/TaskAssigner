package com.example.taskassigner.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.activities.TaskListActivity
import com.example.taskassigner.databinding.ItemCardBinding
import com.example.taskassigner.models.Card
import com.example.taskassigner.models.SelectedMembers

open class CardListItemsAdapter(private val context: Context,
                                private val list: ArrayList<Card>)
    : RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root){
        val tvCardName = binding.tvCardName
        val viewLabelColor = binding.viewLabelColor
        val rvCardSelectedMembersList = binding.rvCardSelectedMembersList
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

        // this below is for recyclerview on item_card.xml
        // which will show images of the current members of the card on TaskListActivity UI
        // check if mAssignedMemberDetailsList in TaskListActivity is not empty
        if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0){
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            for (i in context.mAssignedMemberDetailList.indices){
                for (j in model.assignedTo){
                    if (context.mAssignedMemberDetailList[i].id == j){

                        // create models of SelectedMembers
                        val selectedMember = SelectedMembers(
                            context.mAssignedMemberDetailList[i].id,
                            context.mAssignedMemberDetailList[i].image
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }
            if (selectedMembersList.size > 0 ){
                // if list is not empty, make rv visible so that
                // user can see which members are assigned to that specific card without going inside of the card
                // setup adapter and layoutManager
                holder.rvCardSelectedMembersList.visibility = View.VISIBLE
                holder.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context, 4)
                val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                holder.rvCardSelectedMembersList.adapter = adapter

                // setup onClickListener to each adapter item which will start intent for CardDetailsActivity
                adapter.setOnClickListener(object: CardMemberListItemsAdapter.OnCLickListener{
                    override fun onClick() {
                        if (onClickListener != null){
                            onClickListener!!.onClick(holder.adapterPosition)
                        }
                    }
                })

            }else{
                holder.rvCardSelectedMembersList.visibility = View.GONE
            }
        }

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