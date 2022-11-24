package com.example.taskassigner.firebase

import android.util.Log
import com.example.taskassigner.activities.BaseActivity
import com.example.taskassigner.models.BoardModel
import com.example.taskassigner.models.TaskModel
import com.example.taskassigner.models.UserModel
import com.example.taskassigner.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass: BaseActivity() {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(callback: UserRegistrationCallback, userInfo: UserModel){
        mFireStore.collection(Constants.USERS)
            // create new doc for every user
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                callback.userRegisteredSuccess()
            }.addOnFailureListener {
                e->
                callback.userRegistrationFailure(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun loadUserData(callback: UserDataLoadCallback){
        mFireStore.collection(Constants.USERS)
            // create new doc for every user
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                // get logged in user data in User model
                val loggedInUser = document.toObject(UserModel::class.java)

                if (loggedInUser != null){
                    callback.userDataLoadSuccess(loggedInUser)
                }

            }.addOnFailureListener {
                    e->
                callback.userDataLoadFailed(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun updateUserProfileData(callback: UserDataUpdateCallback, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(callback.javaClass.simpleName, "Profile Data updated.")

                callback.updateDataLoadSuccess()

            }.addOnFailureListener {
                    e->
                callback.updateDataLoadFailed(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun createBoard(callback: CreateBoardCallback, board: BoardModel){
        // create new doc for the board
        mFireStore.collection(Constants.BOARDS)
                // set random doc id
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {

                Log.e(callback.javaClass.simpleName, "Board created successfully.")
                callback.createBoardSuccess()

            }.addOnFailureListener {
                    e->
                callback.createBoardFailed(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun getBoardsList(callback: GetBoardsListCallback){

        // get the board assigned to the current user id
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->

                val boardsList: ArrayList<BoardModel> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(BoardModel::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }

                Log.e(callback.javaClass.simpleName, "Board fetched successfully.")
                callback.getBoardsSuccess(boardsList)

            }.addOnFailureListener { e->
                callback.getBoardsFailed(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun getBoardDetails(callback: GetBoardDetailsCallback, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(callback.javaClass.simpleName, document.toString())

                val boardDetails = document.toObject(BoardModel::class.java)
                callback.getBoardDetailsSuccess(boardDetails!!)

            }.addOnFailureListener { e->
                callback.getBoardDetailsFailed(e.toString())
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    interface UserRegistrationCallback {
        fun userRegisteredSuccess()
        fun userRegistrationFailure(error: String?)
    }

    interface UserDataLoadCallback {
        fun userDataLoadSuccess(user: UserModel)
        fun userDataLoadFailed(error: String?)
    }

    interface UserDataUpdateCallback {
        fun updateDataLoadSuccess()
        fun updateDataLoadFailed(error: String?)
    }

    interface CreateBoardCallback {
        fun createBoardSuccess()
        fun createBoardFailed(error: String?)
    }

    interface GetBoardsListCallback {
        fun getBoardsSuccess(boardsList: ArrayList<BoardModel>)
        fun getBoardsFailed(error: String?)
    }

    interface GetBoardDetailsCallback {
        fun getBoardDetailsSuccess(board: BoardModel)
        fun getBoardDetailsFailed(error: String?)
    }
}