package com.example.taskassigner.activities

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.TaskListItemsAdapter
import com.example.taskassigner.databinding.ActivityTaskListBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.BoardModel
import com.example.taskassigner.models.TaskModel
import com.example.taskassigner.utils.Constants

class TaskListActivity : BaseActivity(),
    FirestoreClass.GetBoardDetailsCallback,
    FirestoreClass.AddUpdateTaskListCallback {
    private var binding: ActivityTaskListBinding? = null
    private var boardDocumentId = ""
    private lateinit var mBoardDetails: BoardModel

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

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = mBoardDetails.name

            binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun createTaskList(taskListName: String){
        // create a new task
        val task = TaskModel(taskListName, FirestoreClass().getCurrentUserId())
        mBoardDetails.taskModelList.add(0, task)
        mBoardDetails.taskModelList.removeAt(mBoardDetails.taskModelList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, taskListName: String, model: TaskModel){
        val task = TaskModel(taskListName, model.createdBy)
        mBoardDetails.taskModelList[position] = task
        mBoardDetails.taskModelList.removeAt(mBoardDetails.taskModelList.size -1 )

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskModelList.removeAt(position)
        mBoardDetails.taskModelList.removeAt(mBoardDetails.taskModelList.size -1 )

        showProgressDialog(resources.getString(R.string.please_wait))

        // update the whole board
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    override fun addUpdateTaskListSuccess(){
        cancelProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }

    override fun addUpdateTaskListFailed(error: String?) {
        cancelProgressDialog()
        Log.e("Error creating board", error.toString())
    }

    override fun getBoardDetailsSuccess(board: BoardModel) {
        // initialize lateinit
        mBoardDetails = board

        cancelProgressDialog()
        setupActionBar()

//        dummy element
        val addTaskList = TaskModel(resources.getString(R.string.add_list))
        board.taskModelList.add(addTaskList)

        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, board.taskModelList)
        binding?.rvTaskList?.adapter = adapter
    }

    override fun getBoardDetailsFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Error occurred", error.toString())
    }
}