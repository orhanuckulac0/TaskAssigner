package com.example.taskassigner.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import com.example.taskassigner.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set splashscreen to fullscreen
        setScreenToFullSize()

        // assign the text font
        val typeFace: Typeface = Typeface.createFromAsset(assets, "Quartist.ttf")
        typeFace.isBold
        binding.tvAppNameIntro.typeface = typeFace

        binding.btnSignUpIntro.setOnClickListener {
            val intent = Intent(this@IntroActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignInIntro.setOnClickListener {
            val intent = Intent(this@IntroActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}