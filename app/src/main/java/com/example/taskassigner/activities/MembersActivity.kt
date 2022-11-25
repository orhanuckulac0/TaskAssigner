package com.example.taskassigner.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.MemberListItemsAdapter
import com.example.taskassigner.databinding.ActivityMembersBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants

class MembersActivity : BaseActivity(), FirestoreClass.GetAssignedMembersList {
    private var binding: ActivityMembersBinding? = null
    private lateinit var mBoarDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            if (Build.VERSION.SDK_INT >= 33) {
                mBoarDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            }else {
                mBoarDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this, mBoarDetails.assignedTo)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = resources.getString(R.string.members)

            binding?.toolbarMembersActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun getAssignedMembersListSuccess(usersList: ArrayList<User>) {
        cancelProgressDialog()

        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, usersList)
        binding?.rvMembersList?.adapter = adapter
    }

    override fun getAssignedMembersListFailed(error: String?) {
        cancelProgressDialog()
        Log.e("Error getting usersList", error.toString())
    }
}