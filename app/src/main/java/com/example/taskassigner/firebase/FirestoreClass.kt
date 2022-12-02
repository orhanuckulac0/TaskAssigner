package com.example.taskassigner.firebase

import android.util.Log
import com.example.taskassigner.activities.BaseActivity
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass: BaseActivity() {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(callback: UserRegistrationCallback, userInfo: User){
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
                val loggedInUser = document.toObject(User::class.java)

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

    fun createBoard(callback: CreateBoardCallback, board: Board){
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

    fun deleteBoard(callback: DeleteBoardCallback, boardID: String){
        mFireStore.collection(Constants.BOARDS)
            .document(boardID)
            .delete()
            .addOnSuccessListener {
                Log.e(callback.javaClass.simpleName, "Board Deleted Successfully")

                callback.deleteBoardCallbackSuccess()
            }.addOnFailureListener {
                    e->
                callback.deleteBoardCallbackFailed(e.toString())
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun getBoardsList(callback: GetBoardsListCallback){

        // get the board assigned to the current user id
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->

                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(Board::class.java)!!
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

                val boardDetails = document.toObject(Board::class.java)!!
                boardDetails.documentId = document.id
                callback.getBoardDetailsSuccess(boardDetails)

            }.addOnFailureListener { e->
                callback.getBoardDetailsFailed(e.toString())
            }
    }

    fun addUpdateTaskList(callback: AddUpdateTaskListCallback, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(callback.javaClass.simpleName, "Tasklist updated successfully")
                callback.addUpdateTaskListSuccess()

            }.addOnFailureListener { e->
                callback.addUpdateTaskListFailed(e.toString())
            }
    }

    fun getAssignedMembersList(callback: GetAssignedMembersList, assignedTo: ArrayList<String>){
        // get users in users collection
        // get users where assignedTo string == id string --- which is userid on Firestore
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.USER_ID, assignedTo)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(callback.javaClass.simpleName, document.documents.toString())
                val usersList: ArrayList<User> = ArrayList()
                for (user in document.documents){
                    val userObj = user.toObject(User::class.java)!!
                    usersList.add(userObj)
                }

                callback.getAssignedMembersListSuccess(usersList)

            }.addOnFailureListener {
                e->
                callback.getAssignedMembersListFailed(e.toString())
            }
    }

    // this fun gets single member details
    fun getMemberDetails(callback: GetMemberDetailsCallback, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document->
                Log.i(callback.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0 ){
                    val user = document.documents[0].toObject(User::class.java)!!
                    callback.getMemberDetailsCallbackSuccess(user)
                }else{
                    callback.getMemberDetailsCallbackNoMemberFound("No such member found.")
                }
            }
    }

    // this fun will update current assignedTo on firestore
    fun assignMemberToBoard(callback: AssignMemberToBoardCallback, board: Board, user: User){
        // to update, we need hashmap
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)

            .addOnSuccessListener {
                callback.assignMemberToBoardCallbackSuccess(user)

            }.addOnFailureListener {
                e->
                callback.assignMemberToBoardCallbackFailed(e.toString())
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
        fun userDataLoadSuccess(user: User)
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

    interface DeleteBoardCallback {
        fun deleteBoardCallbackSuccess()
        fun deleteBoardCallbackFailed(error: String?)
    }

    interface GetBoardsListCallback {
        fun getBoardsSuccess(boardsList: ArrayList<Board>)
        fun getBoardsFailed(error: String?)
    }

    interface GetBoardDetailsCallback {
        fun getBoardDetailsSuccess(board: Board)
        fun getBoardDetailsFailed(error: String?)
    }

    interface AddUpdateTaskListCallback {
        fun addUpdateTaskListSuccess()
        fun addUpdateTaskListFailed(error: String?)
    }

    interface GetAssignedMembersList {
        fun getAssignedMembersListSuccess(usersList: ArrayList<User>)
        fun getAssignedMembersListFailed(error: String?)
    }

    interface GetMemberDetailsCallback {
        fun getMemberDetailsCallbackSuccess(user: User)
        fun getMemberDetailsCallbackFailed(error: String?)
        fun getMemberDetailsCallbackNoMemberFound(error: String)
    }
    interface AssignMemberToBoardCallback {
        fun assignMemberToBoardCallbackSuccess(user: User)
        fun assignMemberToBoardCallbackFailed(error: String?)
    }
}