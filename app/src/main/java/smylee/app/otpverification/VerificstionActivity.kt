package smylee.app.otpverification

import smylee.app.ui.base.BaseActivity
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import smylee.app.NewSignUpActivity
import smylee.app.URFeedApplication
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import kotlinx.android.synthetic.main.activity_verificstion.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.home.HomeViewModel
import smylee.app.login.LoginActivity
import smylee.app.model.OtpVerifyresponse

class VerificstionActivity : BaseActivity() {
    private lateinit var otpViewModel: OtpViewModel
    private lateinit var otpVerifyresponse: OtpVerifyresponse

    var PHONE_NUMBER: String = ""
    var COUNTRY_CODE: String = ""
    var FIRST_NAME: String = ""
    var LAST_NAME: String = ""
    var screenName: String = ""
    private lateinit var viewModel: HomeViewModel
    private var fcmToken: String = ""
    private var shareUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificstion)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        otpViewModel = ViewModelProviders.of(this).get(OtpViewModel::class.java)

        if (intent != null) {
            screenName = intent.getStringExtra("screenName")!!
            if (screenName.contentEquals("preview")) {
                shareUri = intent.getParcelableExtra("shareUri")

                Logger.print("shareUri in verification activity=================$shareUri")
            }
        }

        fcmToken =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.FIREBASE_TOKEN, "")!!

        Logger.print("screenName in otp verification=======================$screenName")
        FIRST_NAME = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.FIRST_NAME_PREF, "")!!

        LAST_NAME = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.LAST_NAME_PREF, "")!!

        PHONE_NUMBER = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.PHONE_NUMBER_PREF, "")!!
        COUNTRY_CODE = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.COUNTRY_CODE_PREF, "")!!

        Handler().postDelayed({
            Utils.showKeyboard(circlePinField, this)
            circlePinField.requestFocus()
        }, 150)

        circlePinField.setOnPinEnteredListener {
            Utils.hideKeyboard(
                circlePinField,
                this
            )
        }

        tv_resend_otp.setOnClickListener {
            RESENDOTP()
        }

        back.setOnClickListener {
            Utils.hideKeyboard(this)
            setResult(Activity.RESULT_CANCELED)
            this.finish()

            /*val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("screenName", "")
            startActivity(intent)
            finish()*/
        }

        btn_submit.setOnClickListener {
            if (circlePinField.text.toString().trim().length == 4) {
                verifyOtp()
            } else {
                Utils.showAlert(this, "", resources.getString(R.string.validate_otp))
            }
        }
    }

    override fun onBackPressed() {
        Utils.hideKeyboard(this)
        setResult(Activity.RESULT_CANCELED)
        this.finish()

    }

    override fun onDestroy() {
        Utils.hideKeyboard(this)

        super.onDestroy()
    }

    override fun onStop() {
        Utils.hideKeyboard(this)

        super.onStop()
    }

    override fun onPause() {
        Utils.hideKeyboard(this)
        super.onPause()
    }

    private fun sendFcmToken(FCM_TOKEN: String) {
        val hashMap = HashMap<String, String>()
        hashMap["device_token"] = FCM_TOKEN
//        val apiName: String = APIConstants.SETUSERDEVICERELATION
        viewModel.SendFcmToken(this, hashMap,true)
            .observe(this, Observer {
                if (it != null) {
                    Logger.print("SETUSERDEVICERELATION Response : $it")
                }
            })
    }

    private fun RESENDOTP() {
        val hashMap = HashMap<String, String>()
        hashMap["phone_number"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.PHONE_NUMBER_PREF, "").toString()
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
                }
            }
        })
    }

    private fun verifyOtp() {
        val hashMap = HashMap<String, String>()
        val otp: String = circlePinField.text.toString().trim()
        hashMap["verify_code"] = otp
        Logger.print("Otp is =======$otp")

        otpViewModel.observeVerifyOTP(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("VerifyOTP response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        Utils.deleteProfileDataFile(this)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_LOGIN, true)
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING, true)
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.IS_ANYTHING_CHANGE_NOTIFICATION, true)
                        Utils.showToastMessage(this, jsonObject["message"].toString())

                        if (!fcmToken.contentEquals("")) {
                            sendFcmToken(fcmToken)
                        }

                        if (FIRST_NAME.contentEquals("") || LAST_NAME.contentEquals("")) {
                            val intent = Intent(this, NewSignUpActivity::class.java)
                            intent.putExtra("screenName", screenName)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        } else {
                            if (screenName.contentEquals("profile") || screenName.contentEquals("notification") || screenName.contentEquals(
                                    "following"
                                ) || screenName.contentEquals("startPost")
                            ) {
                                /*val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("screenName", screenName)
                                startActivity(intent)*/
                                forYouListResponse = ""

                                setResult(Activity.RESULT_OK)
                                finish()
                            } else if (screenName.contentEquals("preview")) {

                                val intent = Intent("shareFormGallery")
                                intent.putExtra("shareUri", shareUri)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                                forYouListResponse = ""

                                setResult(Activity.RESULT_OK)
                                if (LoginActivity.loginActivity != null) {
                                    LoginActivity.loginActivity!!.finish()

                                }
                                finish()
                            } else if (screenName.contentEquals("player") || screenName.contentEquals(
                                    "forYou"
                                ) || screenName.contentEquals("otherprofile") || screenName.contentEquals(
                                    "search"
                                ) || screenName.contentEquals("unfollow") || screenName.contentEquals(
                                    "follow"
                                ) || screenName.contentEquals("setting")
                            ) {
                                forYouListResponse = ""
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            } else {
                                /*val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("screenName", "")
                                startActivity(intent)*/
                                forYouListResponse = ""
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    } else if (jsonObject["code"] == 0) {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    } else if (jsonObject["code"] == 401) {
                    } else {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                } catch (e: Exception) {
                }
            }
        })
    }


}