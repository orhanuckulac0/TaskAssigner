package com.example.taskassigner.activities

import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityBaseBinding
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding

    private var doubleBackToExitPressedOnce = false
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun setScreenToFullSize(){
        // set splashscreen to fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)
        mProgressDialog?.setContentView(R.layout.dialog_progress)
        mProgressDialog?.findViewById<TextView>(R.id.tvProgressBarText)?.text = text
        mProgressDialog?.show()
    }

    fun cancelProgressDialog(){
        mProgressDialog?.dismiss()
    }

    fun doubleBackToExit(){
        // on first click, it will function normally
        if (doubleBackToExitPressedOnce){
            onBackPressedDispatcher.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_LONG
        ).show()

        // if user clicks back button twice within 2seconds, current activity will be closed
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false}, 2000)
    }

    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG)
        val snackBackView = snackBar.view
        snackBackView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()
    }
}