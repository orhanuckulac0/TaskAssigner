package com.example.taskassigner.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.TaskListItemsAdapter
import com.example.taskassigner.databinding.ActivityTaskListBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.Card
import com.example.taskassigner.models.Task
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import com.google.firebase.firestore.ktx.firestoreSettings

class TaskListActivity : BaseActivity(),
    FirestoreClass.GetBoardDetailsCallback,
    FirestoreClass.AddUpdateTaskListCallback,
    FirestoreClass.GetAssignedMembersList {

    private var binding: ActivityTaskListBinding? = null
    private lateinit var boardDocumentId: String
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        // make get task details callback on onResume function,
        // so that I can update screen if data is changed when user is on MembersActivity
        // tested with registerForActivityResult but code is cleaner this way
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.actionMembers -> {
                // pass board details to MembersActivity
                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun createTaskList(taskListName: String){
        // create a new task
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, taskListName: String, model: Task){
        val task = Task(taskListName, model.createdBy, model.cards)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1 )

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1 )

        showProgressDialog(resources.getString(R.string.please_wait))

        // update the whole board
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1 )

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())

        val card = Card(
            cardName,
            FirestoreClass().getCurrentUserId(),
            cardAssignedUsersList
        )

        // if there are other cards, this will be the last one
        // create cardsList
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        // assign the current position task in taskList
        // to the task we create right now with the cardsList
        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        // update the whole board
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMemberDetailList)
        startActivity(intent)
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

    override fun getBoardDetailsSuccess(board: Board) {
        // initialize lateinit
        mBoardDetails = board

        setupActionBar()

//        dummy element
        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, board.taskList)
        binding?.rvTaskList?.adapter = adapter

        // make callback for members list for this specific document because
        // mBoardDetails is initialized in this function above
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    override fun getBoardDetailsFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Error occurred", error.toString())
    }

    override fun getAssignedMembersListSuccess(usersList: ArrayList<User>) {
        mAssignedMemberDetailList = usersList
        Log.i("members", "$mAssignedMemberDetailList")
        cancelProgressDialog()
    }

    override fun getAssignedMembersListFailed(error: String?) {
        Log.i("Error occurred", error.toString())
        cancelProgressDialog()
    }

    override fun onResume() {
        super.onResume()
        // reload data and UI each time activity opens or user comes back
        // be sure not to make this callback on onCreate
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}