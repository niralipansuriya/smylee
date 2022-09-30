package smylee.app.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_setting.*
import org.json.JSONObject
import smylee.app.AppLanguage.LanguageSelectionApp
import smylee.app.ChangePassword.ChangePassword
import smylee.app.Intrest.ChooseYourCategoryIntrestActivity
import smylee.app.Intrest.ChooseYourLanguageIntrest
import smylee.app.Profile.ProfileActivity
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.blockuser.BlockUserActivity
import smylee.app.dialog.CommonAlertDialog
import smylee.app.login.LoginActivity
import smylee.app.login.LoginVIewModel
import smylee.app.model.UpdateProfileResponse
import smylee.app.model.UserLoginResponse
import smylee.app.privacypolicy.PrivacyPolicyActivity
import smylee.app.termscondition.TermsConditionsActivity
import smylee.app.ui.Activity.ChangeMobileConfirmation
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils

class SettingActivity : BaseActivity() {

    private lateinit var loginVIewModel: LoginVIewModel
    private lateinit var viewModel: SettingViewModel
    private var allowNotify: String = "" //M: Male F:Female B:Both
    private lateinit var userprofileresponse: UpdateProfileResponse
    private var languagePref: String = ""
    private lateinit var userLoginResponse: UserLoginResponse
    private var selectedLang: String = ""
    private var categoryPref: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        settingActivity = this
        languagePref = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!
        categoryPref = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.CATEGORY_PREF, "")!!
        loginVIewModel = ViewModelProviders.of(this).get(LoginVIewModel::class.java)

        viewModel = ViewModelProviders.of(this).get(SettingViewModel::class.java)

        back.setOnClickListener {
            onBackPressed()
        }
        languagePref = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!

        ll_block_user.setOnClickListener {
            val intent = Intent(this, BlockUserActivity::class.java)
            startActivity(intent)
        }
        ll_change_psw.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }
        ll_change_mobile_number.setOnClickListener {
            val intent = Intent(this, ChangeMobileConfirmation::class.java)
            startActivity(intent)
        }

        llEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        llAppLanguagePref.setOnClickListener {
            val intent = Intent(this, LanguageSelectionApp::class.java)
            startActivity(intent)
        }
        ll_privacy_policy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
        ll_terms_condition.setOnClickListener {
            val intent = Intent(this, TermsConditionsActivity::class.java)
            startActivity(intent)
        }
        ll_rate_app?.setOnClickListener {
            showRatingReviewDialog()
        }
        val swAllow: String = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.ALLOW_NOTIFY, "").toString()
        if (!swAllow.contentEquals("")) {
            switch_notifications.isChecked = swAllow.contentEquals("1")
        }

        switch_notifications.setOnCheckedChangeListener { _, isChecked ->
            allowNotify = if (isChecked) {
                "1"
            } else {
                "0"
            }
            allowNotifyOrNotAPI(allowNotify)
        }

        ll_chnage_lang.setOnClickListener {
            val intent = Intent(this, ChooseYourLanguageIntrest::class.java)
            intent.putExtra("is_show", true)
            intent.putExtra("is_visible", "0")
            intent.putExtra("screen_name", "")
            intent.putExtra("shareUri", "")

            startActivity(intent)
        }

        ll_chnage_category.setOnClickListener {
            val intent = Intent(this, ChooseYourCategoryIntrestActivity::class.java)
            intent.putExtra("is_show", true)
            intent.putExtra("is_visible", "0")
            startActivity(intent)
        }

        btn_logout.setOnClickListener {
            val alertDialog = object :
                CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    signOutAPICall()
                }

                override fun cancelClicked() {
                }
            }
            alertDialog.initDialog(resources.getString(R.string.logout), resources.getString(R.string.logout_txt))
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }

    private fun allowNotifyOrNotAPI(allowNotify: String) {
        val hashMap = HashMap<String, String>()
        hashMap["allow_notify"] = allowNotify
        viewModel.allowNotifyOrNot(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("allow_notify response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        if (jsonObject.has("data")) {
                            val data = jsonObject.getJSONObject("data")
                            userprofileresponse = Gson().fromJson(data.toString(), object : TypeToken<UpdateProfileResponse>() {}.type)
                            SharedPreferenceUtils.getInstance(this)
                            SharedPreferenceUtils.getInstance(this).setValue(Constants.ALLOW_NOTIFY,
                                userprofileresponse.allow_notify.toString())
                        }
                    } else if (jsonObject["code"] == 2) {
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

    private fun showRatingReviewDialog() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            try {
                if (request.isSuccessful) {
                    Logger.print("request.isSuccessful setting!!!!!!!!!!!!!!!!${request.isSuccessful}")
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener {
                        Logger.print("addOnCompleteListener setting!!!!!!!!!!!!!!!!")
                    }
                    flow.addOnFailureListener {

                        Toast.makeText(
                            this,
                            resources.getString(R.string.alert_rating_review),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else if (!request.isSuccessful) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.alert_rating_review),
                        Toast.LENGTH_LONG
                    ).show()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        request.addOnFailureListener {
            Toast.makeText(
                this,
                resources.getString(R.string.alert_rating_review),
                Toast.LENGTH_LONG
            ).show()

        }
    }


    private fun signOutAPICall() {
        val hashMap = HashMap<String, String>()
        viewModel.signOut(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("SIGNOUT Response : $it")
                val jsonObject = JSONObject(it.toString())
                when (jsonObject.getInt("code")) {
                    1 -> {
                        Utils.deleteProfileDataFile(this)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_LOGIN, false)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.skip_login, false)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.AUTH_TOKEN_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.EMAILPREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.FIRST_NAME_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.LAST_NAME_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.EMAILPREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.USER_REGISTER_STATUS_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_VERIFIED_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.DOB_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.FOLLOWING_COUNT_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.FOLLOWER_COUNT_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.USER_ID_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.BLOCK_USER_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.GENDER_PREF, 0)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.PROFILE_PIC_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.CROPED_PROFILE_PIC, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_BLOCKED_PREF, 0)
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.ALLOW_NOTIFY, "")
                        userProceedAsGuest()
                        sendFcmToken("")
                    }
                    0 -> {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                    else -> {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                }
            }
        })
    }

    private fun sendFcmToken(FCM_TOKEN: String) {
        val hashMap = HashMap<String, String>()
        hashMap["device_token"] = FCM_TOKEN
        loginVIewModel.SendFcmToken(this, hashMap,true).observe(this, Observer {
            if (it != null) {
                Logger.print("SETUSERDEVICERELATION Response : $it")
            }
        })
    }

    private fun userProceedAsGuest() {
        Logger.print("userProceedAsGuest==================" + "callllllllllllll")
        val uID = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.U_ID, "")
        val hashMap = HashMap<String, String>()
        hashMap["user_guid"] = uID.toString()
        loginVIewModel.userProceedAsGuest(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("userProceedAsGuest response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        Utils.deleteProfileDataFile(this)
                        Logger.print("userProceedAsGuest code one=============" + jsonObject["code"].toString())
                        if (jsonObject.has("data")) {
                            Logger.print("userProceedAsGuest data=============" + jsonObject.has("data"))
                            userLoginResponse = Gson().fromJson(jsonObject.getString("data").toString(),
                                object : TypeToken<UserLoginResponse>() {}.type)
                            Logger.print("userLoginResponse===============$userLoginResponse")

                            if (userLoginResponse.getemail_id() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.EMAILPREF,
                                    userLoginResponse.getemail_id()!!)
                            if (userLoginResponse.getisBlocked() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_BLOCKED_PREF,
                                    userLoginResponse.getisBlocked()!!)
                            if (userLoginResponse.getuser_id() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.USER_ID_PREF,
                                    userLoginResponse.getuser_id()!!)
                            if (userLoginResponse.getfirst_name() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.FIRST_NAME_PREF,
                                    userLoginResponse.getfirst_name()!!)
                            if (userLoginResponse.getlast_name() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.LAST_NAME_PREF,
                                    userLoginResponse.getlast_name()!!)
                            if (userLoginResponse.getallow_notify() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.ALLOW_NOTIFY,
                                    userLoginResponse.getallow_notify()!!.toString())
                            if (userLoginResponse.getgender() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.GENDER_PREF,
                                    userLoginResponse.getgender()!!)
                            if (userLoginResponse.user_register_status() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.USER_REGISTER_STATUS_PREF,
                                    userLoginResponse.user_register_status()!!)
                            if (userLoginResponse.getis_verified() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_VERIFIED_PREF,
                                    userLoginResponse.getis_verified()!!)
                            if (userLoginResponse.date_of_birth() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.DOB_PREF,
                                    userLoginResponse.date_of_birth()!!)
                            if (userLoginResponse.no_of_followings() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.FOLLOWING_COUNT_PREF,
                                    userLoginResponse.no_of_followings()!!)
                            if (userLoginResponse.getno_of_followers() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(Constants.FOLLOWER_COUNT_PREF,
                                    userLoginResponse.getno_of_followers()!!)
                            SharedPreferenceUtils.getInstance(this).setValue(Constants.AUTH_TOKEN_PREF,
                                jsonObject.getString("auth_token"))
                            if (!languagePref.contentEquals("")) {
                                updateUserVideoLanguage(languagePref)
                            }
                        }
                    } else if (jsonObject["code"] == 0) {
                        Utils.showToastMessage(this, jsonObject.getString("message"))
                    } else {
                        Utils.showToastMessage(this, jsonObject.getString("message"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.print("error in login ==============" + e.message)
                }
            }
        })
    }

    private fun updateUserVideoLanguage(finalCatIds: String) {
        Logger.print("finalCatIds============$finalCatIds")
        val hashMap = HashMap<String, String>()
        hashMap["language_list"] = finalCatIds
        loginVIewModel.getUpdatedUserLanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("UPDATEVIDEOLANGUAGE Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val languageUpdated: String = jsonObj1.getString("language")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.LANGUAGE_PREF, languageUpdated)
                        selectedLang = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!
                        if (!categoryPref.contentEquals("")) {
                            getUpdatedUserCategorySelection(categoryPref)
                        }
                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    private fun getUpdatedUserCategorySelection(category_list: String) {
        val hashMap = HashMap<String, String>()
        hashMap["category_list"] = category_list
        loginVIewModel.getUpdatedUserCategory(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("UPDATEUSERCATEGORY Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val categoryUpdated: String = jsonObj1.getString("category_list")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.CATEGORY_PREF, categoryUpdated)
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("screen_name", "setting")
                        startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                        finish()
                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                } else {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    companion object {
        var settingActivity: SettingActivity? = null
    }
}