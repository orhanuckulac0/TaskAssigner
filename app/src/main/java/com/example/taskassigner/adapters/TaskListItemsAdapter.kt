package com.example.taskassigner.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.taskassigner.activities.TaskListActivity
import com.example.taskassigner.databinding.ItemTaskBinding
import com.example.taskassigner.models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemsAdapter(private val context: Context,
                                private val list: ArrayList<Task>):
    RecyclerView.Adapter<TaskListItemsAdapter.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    class ViewHolder(binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root){
        val tvAddTaskList = binding.tvAddTaskList
        val tvTaskListTitle = binding.tvTaskListTitle
        val tvAddCard = binding.tvAddCard

        val llTaskItem = binding.llTaskItem
        val llTitleView = binding.llTitleView

        val cvAddTaskListName = binding.cvAddTaskListName
        val cvEditTaskListName = binding.cvEditTaskListName
        val cvAddCard = binding.cvAddCard

        val ibDoneListName = binding.ibDoneListName
        val ibCloseListName = binding.ibCloseListName
        val ibEditListName = binding.ibEditListName
        val ibCloseEditableView = binding.ibCloseEditableView
        val ibDoneEditListName = binding.ibDoneEditListName
        val ibDeleteList = binding.ibDeleteList
        val ibCloseCardName = binding.ibCloseCardName
        val ibDoneCardName = binding.ibDoneCardName

        val etTaskListName = binding.etTaskListName
        val etEditTaskListName = binding.etEditTaskListName
        val etCardName = binding.etCardName

        val rvCardList = binding.rvCardList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // view holder should be 0.7 * the width of the parent screen
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins(
            // 15 margin towards left, 40 margin towards right
            (15.toDp().toPx()), 0, (40.toDp().toPx()), 0
        )
        view.root.layoutParams = layoutParams
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        // if there's no entries in the list, tv should be visible
        if (position == list.size-1){
            holder.tvAddTaskList.visibility = View.VISIBLE

            // make everything about cards GONE
            holder.llTaskItem.visibility = View.GONE
        }else{
            // vise versa
            holder.tvAddTaskList.visibility = View.GONE
            holder.llTaskItem.visibility = View.VISIBLE
        }
        
        holder.tvTaskListTitle.text = model.title

        holder.tvAddTaskList.setOnClickListener {
            holder.tvAddTaskList.visibility = View.GONE
            holder.cvAddTaskListName.visibility = View.VISIBLE
        }

        holder.ibCloseListName.setOnClickListener {
            holder.tvAddTaskList.visibility = View.VISIBLE
            holder.cvAddTaskListName.visibility = View.GONE
        }

        holder.ibDoneListName.setOnClickListener {
            val listName = holder.etTaskListName.text.toString()
            // if user input is not empty,
            // create new taskList
            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.createTaskList(listName)
                }
            }else{
                Toast.makeText(context, "Please Enter List name", Toast.LENGTH_LONG).show()
            }
        }

        // when clicked to edit button
        holder.ibEditListName.setOnClickListener {
            holder.etEditTaskListName.setText(model.title)

            holder.llTitleView.visibility = View.GONE
            holder.cvEditTaskListName.visibility = View.VISIBLE
        }


        // closing the edit current list name UI
        holder.ibCloseEditableView.setOnClickListener {
            holder.llTitleView.visibility = View.VISIBLE
            holder.cvEditTaskListName.visibility = View.GONE
        }

        // update the current list
        holder.ibDoneEditListName.setOnClickListener {
            val editedListName = holder.etEditTaskListName.text.toString()
            if (editedListName.isNotEmpty()){
                if (context is TaskListActivity){
                    holder.llTitleView.visibility = View.VISIBLE
                    holder.cvEditTaskListName.visibility = View.GONE
                    context.updateTaskList(position, editedListName ,model)
                }
            }else{
                Toast.makeText(context, "Please Enter List name", Toast.LENGTH_LONG).show()
            }
        }

        // delete the current list
        holder.ibDeleteList.setOnClickListener {
            alertDialogForDeleteList(position, model.title)
        }

        // enable add card name cardView
        holder.tvAddCard.setOnClickListener {
            holder.tvAddCard.visibility= View.GONE
            holder.cvAddCard.visibility = View.VISIBLE
        }

        // close add card cardView
        holder.ibCloseCardName.setOnClickListener {
            holder.tvAddCard.visibility= View.VISIBLE
            holder.cvAddCard.visibility = View.GONE
        }

        // save card name cardView text
        holder.ibDoneCardName.setOnClickListener {

            val cardName = holder.etCardName.text.toString()
            if (cardName.isNotEmpty()){
                if (context is TaskListActivity){
                    holder.tvAddCard.visibility= View.VISIBLE
                    holder.cvAddCard.visibility = View.GONE
                    context.addCardToTaskList(position, cardName)
                }
            }else{
                Toast.makeText(context, "Please Enter List name", Toast.LENGTH_LONG).show()
            }
        }

        // setup cards recyclerview with CardsListItemsAdapter
        holder.rvCardList.layoutManager = LinearLayoutManager(context)
        holder.rvCardList.setHasFixedSize(true)
        val adapter = CardListItemsAdapter(context, model.cards)
        holder.rvCardList.adapter = adapter

        adapter.setOnClickListener(object: CardListItemsAdapter.OnClickListener{
            override fun onClick(cardPosition: Int) {
                if (context is TaskListActivity){
                    // call the cardDetails fun on TaskListActivity which will start intent for CardDetailsActivity
                    context.cardDetails(holder.adapterPosition, cardPosition)
                }
            }
        })

        val helper = ItemTouchHelper(
            object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    Collections.swap(list[position].cards, fromPos, toPos)
                    adapter.notifyItemMoved(fromPos, toPos)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    // when drag and drop stops, call for updating the DB and UI
                    (context as TaskListActivity)
                        .updateCardsInTaskList(position, list[position].cards)
                }

            }
        )
        helper.attachToRecyclerView(holder.rvCardList)
    }

    override fun getItemCount(): Int {
            return list.size
        }

    private fun alertDialogForDeleteList(position: Int, title: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()

            // delete the current TaskList
            if (context is TaskListActivity){
                context.deleteTaskList(position)
                Toast.makeText(context, "List Deleted", Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton("No") { dialogInterface, which->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // allows us to get the density of the screen and convert it in to INT
    // get density pixel from pixel
    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    // allows us to get the pixel of the screen and convert it in to INT
    // get pixel from the density pixel
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

}