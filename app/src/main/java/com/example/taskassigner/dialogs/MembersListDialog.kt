package com.example.taskassigner.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskassigner.R
import com.example.taskassigner.adapters.MemberListItemsAdapter
import com.example.taskassigner.models.User

abstract class MembersListDialog(context: Context,
                                 private val memberList: ArrayList<User>,
                                 private val title: String = "")
    : Dialog(context) {

    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = View.inflate(context, R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        // setup dialog title
        view.findViewById<TextView>(R.id.tvTitle).text = title

        // check the members list size
        // if there are members, setup layoutManager and adapter
        if (memberList.size > 0 ){
            val rvList = view.rootView.findViewById<RecyclerView>(R.id.rvList)

            rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, memberList)
            rvList.adapter = adapter

            adapter!!.setOnClickListener(object : MemberListItemsAdapter.OnItemClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }
    protected abstract fun onItemSelected(user: User, acton: String)
}