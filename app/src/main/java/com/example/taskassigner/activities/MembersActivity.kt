package com.example.taskassigner.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.MemberListItemsAdapter
import com.example.taskassigner.databinding.ActivityMembersBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants


class MembersActivity : BaseActivity(),
    FirestoreClass.GetAssignedMembersList,
    FirestoreClass.GetMemberDetailsCallback,
    FirestoreClass.AssignMemberToBoardCallback {

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
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

    // BELOW FOR ADDING NEW MEMBERS TO A TASK CALLBACK RESPONSE
    override fun getMemberDetailsCallbackSuccess(user: User) {
        cancelProgressDialog()
        // add the new member user to assignedTo list
        mBoarDetails.assignedTo.add(user.id)

        // start the callback for updating firestore db
        FirestoreClass().assignMemberToBoard(this, mBoarDetails, user)

        // to update UI with new member added
        FirestoreClass().getAssignedMembersList(this, mBoarDetails.assignedTo)
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
        Toast.makeText(this, "New Member Added!", Toast.LENGTH_LONG).show()
    }

    override fun assignMemberToBoardCallbackFailed(error: String?) {
        Log.i("Error adding member", error.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}