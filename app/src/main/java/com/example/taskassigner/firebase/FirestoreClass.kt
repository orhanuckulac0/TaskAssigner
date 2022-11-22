package com.example.taskassigner.firebase

import android.util.Log
import com.example.taskassigner.models.UserModel
import com.example.taskassigner.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

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
                Log.e(callback.javaClass.simpleName, "Error", e)
            }
    }

    fun updateUserProfileData(callback: UserDataUpdateCallback, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            // create new doc for every user
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(callback.javaClass.simpleName, "Profile Data updated.")

                callback.updateDataLoadSuccess()

            }.addOnFailureListener {
                    e->
                Log.e(callback.javaClass.simpleName, "Error", e)
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
}