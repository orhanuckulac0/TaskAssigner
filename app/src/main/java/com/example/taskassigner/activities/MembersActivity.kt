package com.example.taskassigner.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.BoardMemberListItemsAdapter
import com.example.taskassigner.databinding.ActivityMembersBinding
import com.example.taskassigner.fcm.MyFirebaseMessagingService
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.*
import com.example.taskassigner.utils.Constants
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MembersActivity : BaseActivity(),
    FirestoreClass.GetAssignedMembersList,
    FirestoreClass.GetMemberDetailsCallback,
    FirestoreClass.AssignMemberToBoardCallback,
    FirestoreClass.DeleteMemberFromBoardCallback {

    private var binding: ActivityMembersBinding? = null
    private lateinit var mBoardDetails: Board
    private lateinit var mCurrentUserID: String
    private val mTopicAssignToBoard = "/assignMemberToBoard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            if (Build.VERSION.SDK_INT >= 33) {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            }else {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }
        }
        mCurrentUserID = FirestoreClass().getCurrentUserId()

        MyFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().subscribeToTopic(mTopicAssignToBoard)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(ContentValues.TAG, "Response: $response")
            } else {
                Log.e(ContentValues.TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(ContentValues.TAG, e.toString())
        }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuBtn = menu!!.findItem(R.id.actionAddMember)
        if (mCurrentUserID != mBoardDetails.createdByID){
            menuBtn.isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.actionAddMember -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_search_member, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)

        val editText = dialogView.findViewById<View>(R.id.etEmailSearchMember) as EditText

        dialogBuilder.setPositiveButton("Yes") { _, _->
            val email = editText.text.toString()
            if (email.isNotEmpty()){
                // if email is not empty, start get member detail callback
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)

            }else{
                Toast.makeText(this,"Please enter an email address.", Toast.LENGTH_LONG).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { _, _->
        }
            .create().show()
    }

    private fun dialogDeleteMemberFromBoard(user: User){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Delete Member From Board")
        dialogBuilder.setMessage("Are you sure you want to delete ${user.name} from the board?\n" +
                "This will cause the removal of this member from all cards and all tasks within this board.")
        dialogBuilder.setCancelable(true)

        dialogBuilder.setPositiveButton("Yes") { _, _->
            mBoardDetails.assignedTo.remove(user.id)
            this.runOnUiThread {
                showProgressDialog(resources.getString(R.string.please_wait))
            }
            FirestoreClass().deleteMemberFromBoard(this, mBoardDetails)
        }

        dialogBuilder.setNegativeButton("Cancel") { _, _->
        }
            .create()
            .show()
    }


    override fun getAssignedMembersListSuccess(usersList: ArrayList<User>) {
        cancelProgressDialog()

        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = BoardMemberListItemsAdapter(
            this,
            usersList,
            mBoardDetails.createdByID,
            mCurrentUserID
        )

        binding?.rvMembersList?.adapter = adapter

        adapter.setOnClickListener(object: BoardMemberListItemsAdapter.OnItemClickListener{
            override fun onClick(user: User) {
                dialogDeleteMemberFromBoard(user)
            }
        })
    }

    override fun getAssignedMembersListFailed(error: String?) {
        cancelProgressDialog()
        Log.e("Error getting usersList", error.toString())
    }

    // BELOW FOR ADDING NEW MEMBERS TO A TASK CALLBACK RESPONSE
    override fun getMemberDetailsCallbackSuccess(user: User) {
        cancelProgressDialog()
        // add the new member user to assignedTo list
        mBoardDetails.assignedTo.add(user.id)

        // start the callback for updating firestore db
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)

        // to update UI with new member added
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    @SuppressLint("LongLogTag")
    override fun getMemberDetailsCallbackFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Error getting member details", error.toString())
    }

    override fun getMemberDetailsCallbackNoMemberFound(error: String) {
        cancelProgressDialog()
        showErrorSnackBar(error)
    }
    // // // //

    // TO ADD MEMBER TO DB CALLBACK RESPONSE
    override fun assignMemberToBoardCallbackSuccess(user: User) {
        cancelProgressDialog()
        Log.i("User FCM token: ", user.fcmToken)
        // send the notification to assigned member
        PushNotification(
            NotificationData(Constants.ADDED_TO_BOARD, "${mBoardDetails.createdBy} has added you to a new board. " +
                    "Click this to open TaskAssigner."),
            user.fcmToken
        ).also {
            sendNotification(it)
        }
    }

    override fun assignMemberToBoardCallbackFailed(error: String?) {
        Log.i("Error adding member", error.toString())
        cancelProgressDialog()
    }

    override fun deleteMemberFromBoardSuccess() {
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    override fun deleteMemberFromBoardFailed(error: String?) {
        cancelProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}