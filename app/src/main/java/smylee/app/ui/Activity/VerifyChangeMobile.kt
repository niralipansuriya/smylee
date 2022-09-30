package smylee.app.ui.Activity

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import smylee.app.retrofitclient.APIConstants
import smylee.app.URFeedApplication
import smylee.app.otpverification.OtpViewModel
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import kotlinx.android.synthetic.main.verify_change_mobile.*
import org.json.JSONObject
import smylee.app.R

class VerifyChangeMobile : BaseActivity() {
    private lateinit var otpViewModel: OtpViewModel

    private var changePhoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_change_mobile)
        otpViewModel = ViewModelProviders.of(this).get(OtpViewModel::class.java)

        if (intent != null) {
            changePhoneNumber = intent.getStringExtra("phone_number")!!
            Logger.print("phone_number===============$changePhoneNumber")
        }

        Handler().postDelayed({
            Utils.showKeyboard(circlePinFieldVerify, this)
            circlePinFieldVerify.requestFocus()
        }, 150)

        circlePinFieldVerify.setOnPinEnteredListener {
            Utils.hideKeyboard(circlePinFieldVerify,this)
        }
        btnVerify.setOnClickListener {
            if (circlePinFieldVerify.text.toString().trim().length == 4) {
                verifyOtp()
            } else {
                Utils.showAlert(this, "", resources.getString(R.string.validate_otp))
            }
        }

        resendOtp.setOnClickListener {
            resendOTP()
        }
    }

    private fun verifyOtp() {
        val hashMap = HashMap<String, String>()
        val otpResend: String = circlePinFieldVerify.text.toString().trim()
        hashMap["country_code"] = APIConstants.COUNTRY_CODE
        hashMap["phone_number"] = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.CHANGEDMOBILENUMBER, "")!!
        hashMap["otp"] = otpResend

        otpViewModel.observeVerifyChangeMobile(this, hashMap).observe(this, Observer {
            if (it != null) {
                try {
                    val jsonObject = JSONObject(it.toString())
                    when {
                        jsonObject["code"] == 1 -> {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                            onBackPressed()
                        }
                        jsonObject["code"] == 0 -> {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                        /*jsonObject["code"] == 401 -> {
                        }*/
                        else -> {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun resendOTP() {
        val hashMap = HashMap<String, String>()
        hashMap["phone_number"] = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.CHANGEDMOBILENUMBER, "")!!
        hashMap["country_code"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.COUNTRY_CODE_PREF, "").toString()

        otpViewModel.observeResendOTP(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("observeResendOTP response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    } else {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }
}