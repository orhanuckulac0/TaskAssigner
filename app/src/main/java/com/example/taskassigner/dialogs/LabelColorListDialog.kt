package com.example.taskassigner.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.R
import com.example.taskassigner.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(context: Context,
                                    private var list: ArrayList<String>,
                                    private val title: String="",
                                    private var mSelectedColor: String = ""): Dialog(context) {
    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = View.inflate(context, R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    // setup rv for custom dialog which will show only colors to be selected
    private fun setupRecyclerView(view: View){
        val rvList = view.rootView.findViewById<RecyclerView>(R.id.rvList)
        val tvTitle = view.rootView.findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = title
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}