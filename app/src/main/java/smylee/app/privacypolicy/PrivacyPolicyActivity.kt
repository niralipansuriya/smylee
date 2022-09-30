package smylee.app.privacypolicy

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import smylee.app.utils.Constants
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import smylee.app.R

class PrivacyPolicyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        iv_back_privacy.setOnClickListener {
            onBackPressed()
        }
        wb_privacy.settings.javaScriptEnabled = true
        wb_privacy.loadUrl(Constants.PRIVACY_POLICY_URL)
    }
}