package smylee.app.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.ivVideoImage
import kotlinx.android.synthetic.main.activity_login.playerView
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.home.HomeActivity
import smylee.app.home.HomeViewModel
import smylee.app.model.UserLoginResponse
import smylee.app.otpverification.VerificstionActivity
import smylee.app.setting.SettingActivity
import smylee.app.termscondition.TermsConditionsActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import smylee.app.utils.Utils.isTextEmpty
import java.util.*
import kotlin.collections.HashMap


class LoginActivity : BaseActivity() {

    /*private val SPLASH_TIME_OUT = 5000L
    private val list = ArrayList<Topic>()
    var registered_by: String? = null*/

    private lateinit var loginVIewModel: LoginVIewModel
    private var deviceId: String = ""

    private lateinit var userLoginResponse: UserLoginResponse
    private var isLogin: Boolean = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var languagePref: String = ""

    //    private var selectedLang: String = ""
    private var simpleExoPlayer: SimpleExoPlayer? = null

    val bundle = Bundle()
    private var uniqueId: String = ""
    private var language: String = ""

    private var screenName: String = ""
    private var uniqueIdPref: String = ""
    private var categoryPref: String = ""
    private lateinit var viewModel: HomeViewModel

    var shareUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        loginActivity = this
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        try {
            val defaultDataSourceFactory =
                DefaultDataSourceFactory(this, Util.getUserAgent(this, "smylee"))
            val extractorMediaSource: ExtractorMediaSource =
                ExtractorMediaSource.Factory(defaultDataSourceFactory).createMediaSource(
                    RawResourceDataSource.buildRawResourceUri(R.raw.splash_video_final)
                )

            simpleExoPlayer?.prepare(extractorMediaSource)
            playerView?.player = simpleExoPlayer
            simpleExoPlayer!!.playWhenReady = true
            simpleExoPlayer?.seekTo(0)
            simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            simpleExoPlayer?.addListener(listener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ivVideoImage.setImageResource(R.drawable.login_logo_final)
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        uniqueId = UUID.randomUUID().toString()

        languagePref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!
        categoryPref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.CATEGORY_PREF, "")!!

        val sb: Spannable = SpannableString(resources.getString(R.string.agree1))

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                startActivity(Intent(this@LoginActivity, TermsConditionsActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.purple)
                ds.isUnderlineText = false
            }
        }
        sb.setSpan(clickableSpan, 25, 43, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvTerms?.text = sb
        tvTerms?.movementMethod = LinkMovementMethod.getInstance()


        Logger.print("uniqueId====================$uniqueId")
        if (intent != null) {
            if (intent.hasExtra("screen_name")) {
                screenName = intent.getStringExtra("screen_name")!!
                if (screenName.contentEquals("preview")) {
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this)
                        .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
                    if (currentLanguage.contentEquals("")) {
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_CODE_PREF, "EN")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_APP_ID, "English")
                    }

                    shareUri = intent.getParcelableExtra("shareUri")

                    Logger.print("shareUri=====================$shareUri")
                }
            }
        }

        edt_phone_number.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    //got focus
                    Logger.print("keyBoard is opne !!!!!!!!!!!!!!!!!!!!!!")
                } else {
                    //lost focus
                    Logger.print("keyBoard is closed !!!!!!!!!!!!!!!!!!!!!!")
                }
            }
        })

        Logger.print("screen_name=====$screenName")

        loginVIewModel = ViewModelProviders.of(this).get(LoginVIewModel::class.java)
        uniqueIdPref = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.U_ID, "")!!
        if (uniqueIdPref.contentEquals("")) {
            uniqueId = UUID.randomUUID().toString()
        }

        if (!uniqueId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.U_ID, uniqueId)
        }

        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        if (!deviceId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.DEVICE_ID, deviceId)
        }
        Logger.print("DEVICE_ID=============", deviceId)

        isLogin = SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)
        language =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!


        btn_login.setOnClickListener {
            if (isTextEmpty(edt_phone_number)) {
                Utils.showAlert(
                    context = this@LoginActivity,
                    title = "",
                    message = getString(R.string.fill_phone)
                )
            } else if (edt_phone_number.text.toString().trim().length != 10) {
                Utils.showAlert(
                    context = this@LoginActivity,
                    title = "",
                    message = getString(R.string.validate_number)
                )
            } else if (!Utils.isValidMobile(edt_phone_number.text.toString().trim())) {
                Utils.showAlert(
                    context = this@LoginActivity,
                    title = "",
                    message = getString(R.string.validate_number)
                )
            } else if (edt_phone_number.text.toString()
                    .startsWith("0") || edt_phone_number.text.toString()
                    .startsWith("1") || edt_phone_number.text.toString()
                    .startsWith("2") || edt_phone_number.text.toString()
                    .startsWith("3") || edt_phone_number.text.toString()
                    .startsWith("4") || edt_phone_number.text.toString().startsWith("5")
            ) {
                Utils.showAlert(
                    context = this@LoginActivity,
                    title = "",
                    message = getString(R.string.validate_number)
                )

            } else if (!cbAgree.isChecked) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.agree_validation)
                )
            } else {
                signInUsingNumber()
            }
        }
    }

    var listener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.i("PlayerFragment", "isPlaying $isPlaying")
            if (isPlaying) {
                ivVideoImage.visibility = View.GONE
                // simpleExoPlayer?.seekTo(0)
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
        }
    }

    private fun signInUsingNumber() {
        val hashMap = HashMap<String, String>()
        hashMap["user_guid"] =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.U_ID, "").toString()
        hashMap["phone_number"] = edt_phone_number.text.toString().trim()
        hashMap["country_code"] = "91"

        loginVIewModel.observeSignINAPI(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("observeSignINAPI response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        Logger.print("code one=============" + jsonObject["code"].toString())

                        if (jsonObject.has("data")) {
                            Logger.print("data=============" + jsonObject.has("data"))

                            userLoginResponse =
                                Gson().fromJson(jsonObject.getString("data").toString(), object :
                                    TypeToken<UserLoginResponse>() {}.type)

                            Logger.print("userLoginResponse===============$userLoginResponse")
                            //SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_LOGIN, true)
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.skip_login, false)
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.AUTH_TOKEN_PREF,
                                jsonObject.getString("auth_token")
                            )

                            if (userLoginResponse.getemail_id() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.EMAILPREF,
                                    userLoginResponse.getemail_id()!!
                                )
                            if (userLoginResponse.getisBlocked() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.IS_BLOCKED_PREF,
                                    userLoginResponse.getisBlocked()!!
                                )
                            if (userLoginResponse.getuser_id() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.USER_ID_PREF,
                                    userLoginResponse.getuser_id()!!
                                )
                            if (userLoginResponse.getphone_number() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.PHONE_NUMBER_PREF,
                                    userLoginResponse.getphone_number()!!
                                )
                            if (userLoginResponse.getcountry_code() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.COUNTRY_CODE_PREF,
                                    userLoginResponse.getcountry_code()!!
                                )
                            if (userLoginResponse.getfirst_name() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FIRST_NAME_PREF,
                                    userLoginResponse.getfirst_name()!!
                                )
                            if (userLoginResponse.getlast_name() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.LAST_NAME_PREF,
                                    userLoginResponse.getlast_name()!!
                                )
                            if (userLoginResponse.getcategory_list() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.CATEGORY_PREF,
                                    userLoginResponse.getcategory_list()!!
                                )
                            if (userLoginResponse.language() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.LANGUAGE_PREF,
                                    userLoginResponse.language()!!
                                )
                            if (userLoginResponse.getallow_notify() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.ALLOW_NOTIFY,
                                    userLoginResponse.getallow_notify()!!.toString()
                                )
                            if (userLoginResponse.getgender() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.GENDER_PREF,
                                    userLoginResponse.getgender()!!
                                )
                            if (userLoginResponse.user_register_status() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.USER_REGISTER_STATUS_PREF,
                                    userLoginResponse.user_register_status()!!
                                )
                            if (userLoginResponse.getis_verified() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.IS_VERIFIED_PREF,
                                    userLoginResponse.getis_verified()!!
                                )
                            if (userLoginResponse.date_of_birth() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.DOB_PREF,
                                    userLoginResponse.date_of_birth()!!
                                )
                            if (userLoginResponse.no_of_followings() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWING_COUNT_PREF,
                                    userLoginResponse.no_of_followings()!!
                                )
                            if (userLoginResponse.getno_of_followers() != null)
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.FOLLOWER_COUNT_PREF,
                                        userLoginResponse.getno_of_followers()!!
                                    )
                            if (userLoginResponse.getuser_id() != null) {
                                val bundle = Bundle()
                                bundle.putString(
                                    FirebaseAnalytics.Param.ITEM_ID,
                                    userLoginResponse.getuser_id()
                                )
                                bundle.putString(
                                    FirebaseAnalytics.Param.ITEM_NAME,
                                    userLoginResponse.getfirst_name()
                                )
                                firebaseAnalytics.logEvent(
                                    FirebaseAnalytics.Event.SELECT_CONTENT,
                                    bundle
                                )
                            }

                            val intent = Intent(this, VerificstionActivity::class.java)
                            intent.putExtra("screenName", screenName)
                            intent.putExtra("shareUri", shareUri)
                            startActivityForResult(intent, Constants.OTP_ACTIVITY_REQUEST_CODE)
//                            finish()
                        }
                    } else if (jsonObject["code"] == 0) {
                        Utils.showToastMessage(this, jsonObject.getString("message"))
                    } else {
                        Utils.showToastMessage(this@LoginActivity, jsonObject.getString("message"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.print("error in login ==============" + e.message)
                }
            }
        })
    }

    override fun onBackPressed() {
        if (screenName.contentEquals("notification") || screenName.contentEquals("profile") || screenName.contentEquals(
                "startPost"
            )
        ) {
            this.finish()
            super.onBackPressed()
            // userProceedAsGuest()

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("screenName", "")
            startActivity(intent)
            finish()
        } else if (screenName.contentEquals("setting")) {
            this.finish()
            //userProceedAsGuest()
        } else {
            ///userProceedAsGuest()
            this.finish()
        }
    }

    companion object {
        var loginActivity: LoginActivity? = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.OTP_ACTIVITY_REQUEST_CODE) {
            if (screenName == "setting") {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("screenName", "")
                startActivity(intent)
                finish()
            } else {
                setResult(resultCode)
                finish()
            }
        }
    }
}