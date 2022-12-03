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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.concurrent.thread


class MembersActivity : BaseActivity(),
    FirestoreClass.GetAssignedMembersList,
    FirestoreClass.GetMemberDetailsCallback,
    FirestoreClass.AssignMemberToBoardCallback {

    private var binding: ActivityMembersBinding? = null
    private lateinit var mBoardDetails: Board

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

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo)
    }

    private fun sendNotificationToUser(createdBy: String, boardName: String, token: String): String{
        var result = ""
        thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val writer = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()

                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE, "You have been assigned to a new board by $createdBy")

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                writer.writeBytes(jsonRequest.toString())
                writer.flush()
                writer.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line=it } != null){
                            stringBuilder.append(line+"\n")
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch (e: IOException){
                e.printStackTrace()
            }catch (e: SocketTimeoutException){
                result = "Connection Time out"
            }catch (e: java.lang.Exception){
                result = "Error: ${e.message}"
            }finally {
                connection?.disconnect()
            }
        }
        return result
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
        runOnUiThread {
            Toast.makeText(this,"Member added successfully.", Toast.LENGTH_LONG).show()
        }
        sendNotificationToUser(mBoardDetails.createdBy, mBoardDetails.name, user.fcmToken)
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