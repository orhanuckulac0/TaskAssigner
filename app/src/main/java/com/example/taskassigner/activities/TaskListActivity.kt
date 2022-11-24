package com.example.taskassigner.activities

import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityTaskListBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.BoardModel
import com.example.taskassigner.utils.Constants

class TaskListActivity : BaseActivity(), FirestoreClass.GetBoardDetailsCallback {
    private var binding: ActivityTaskListBinding? = null
    private var boardDocumentId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    private fun setupActionBar(title: String){
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = title

            binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun getBoardDetailsSuccess(board: BoardModel) {
        cancelProgressDialog()
        setupActionBar(board.name)
    }

    override fun getBoardDetailsFailed(error: String?) {
        cancelProgressDialog()
    }
}