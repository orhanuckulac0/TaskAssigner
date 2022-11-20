package com.example.taskassigner.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivitySignInBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.UserModel
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : BaseActivity(), FirestoreClass.UserLoginCallback {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set sign up activity to fullscreen
        setScreenToFullSize()

        setSupportActionBar(binding.toolbarSignInActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_black_24dp)
        binding.toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.i("Current User", "$currentUser")
    }

    private fun signInUser(){
        val email = binding.etEmailSignIn.text.toString().trim { it <= ' '}
        val password = binding.etPasswordSignIn.text.toString().trim { it <= ' '}

        if (validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    cancelProgressDialog()

                    if (task.isSuccessful) {
                        FirestoreClass().signInUser(this)

                        // Sign in success
                        Toast.makeText(baseContext, "Sign in success.",
                            Toast.LENGTH_SHORT).show()

                    }else if (!task.isSuccessful){
                        try {
                            throw task.exception!!

                        }catch (e: FirebaseAuthInvalidCredentialsException){
                            e.printStackTrace()
                            Toast.makeText(
                                baseContext, "${e.message}.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }catch (e: FirebaseAuthInvalidUserException){
                            e.printStackTrace()
                            Toast.makeText(
                                baseContext, "There is no user record corresponding to this email address.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }catch (e: Exception){
                            e.printStackTrace()
                            cancelProgressDialog()
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Sign in failed.",
                                Toast.LENGTH_SHORT).show()
                            Log.i("Generic error", "${e.message}")
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Unknown error, please try again.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter an email")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter a password")
                false
            }else -> {
                true
            }
        }
    }

    override fun userLoginSuccess(user: UserModel) {
        cancelProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun userLoginFailure(error: String?) {
        TODO("Not yet implemented")
    }
}
