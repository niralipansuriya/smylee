package smylee.app.termscondition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import smylee.app.utils.Constants
import kotlinx.android.synthetic.main.activity_terms_conditions.*
import smylee.app.R

class TermsConditionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)

        iv_back_terms.setOnClickListener {
            onBackPressed()
        }
        wb_terms.settings.javaScriptEnabled = true
        wb_terms.loadUrl(Constants.TERMS_AND_CONDITION_URL)
    }
}