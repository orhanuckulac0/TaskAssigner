package com.example.taskassigner.activities

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // set sign up activity to fullscreen
        setScreenToFullSize()

        setSupportActionBar(binding.toolbarSignUpActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_black_24dp)
        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name: String = binding.etNameSignUp.text.toString().trim { it <= ' '}
        val email: String = binding.etEmailSignUp.text.toString().trim { it <= ' '}
        val password: String = binding.etPasswordSignUp.text.toString()

        if (validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            // register the user
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->

                    cancelProgressDialog()

                    if (task.isSuccessful) {

                        // Sign up success
                        Log.d(TAG, "createUserWithEmail:success")
                        val firebaseUser = auth.currentUser
                        Log.i("User", "${firebaseUser?.email}")

                        Toast.makeText(
                            baseContext, "Authentication successful. User registered.",
                            Toast.LENGTH_SHORT
                        ).show()
                        signOutCurrentUser(auth)  // for now, test
                        finish()  // for now, test

                    }else if (!task.isSuccessful){
                        try {
                            throw task.exception!!
                        }
                        catch (e: FirebaseAuthWeakPasswordException){
                            Toast.makeText(
                                baseContext, "Password should be at least 6 characters.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }catch (e: FirebaseAuthInvalidCredentialsException){
                            Toast.makeText(
                                baseContext, "The email address is invalid.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }catch (e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                baseContext, "This user already exists..",
                                Toast.LENGTH_SHORT
                            ).show()
                        }catch(e: Exception){
                            e.printStackTrace()
                            Toast.makeText(
                                baseContext, "Unknown error has occurred, please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        Toast.makeText(
                            baseContext, "An unknown error has occurred, please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter a name")
                false
            }
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
}