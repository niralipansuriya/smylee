package smylee.app.ui.Activity

import smylee.app.ui.base.BaseActivity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_change_mobile_confirmation.*
import smylee.app.R


class ChangeMobileConfirmation : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_mobile_confirmation)

        back.setOnClickListener {
            onBackPressed()
        }

        btnProceed.setOnClickListener {
            val intent = Intent(this, ChangeMobile::class.java)
            startActivity(intent)
            finish()
        }
    }
}