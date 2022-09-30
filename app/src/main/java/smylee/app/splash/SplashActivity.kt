package smylee.app.splash

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
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
import com.google.common.reflect.TypeToken
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import smylee.app.Intrest.ChooseYourLanguageIntrest
import smylee.app.NewSignUpActivity
import smylee.app.R
import smylee.app.VideoLanguageSelection.VideoSelectionViewModel
import smylee.app.home.HomeActivity
import smylee.app.home.HomeViewModel
import smylee.app.login.LoginVIewModel
import smylee.app.model.UserLoginResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap

class SplashActivity : BaseActivity() {
    private var isLogin: Boolean = false
    private var uniqueId: String = ""
    private var uniqueIdPref: String = ""
    private lateinit var loginVIewModel: LoginVIewModel
    private var deviceId: String = ""
    private var languagePref: String = ""
    private var categoryPref: String = ""
    private lateinit var userLoginResponse: UserLoginResponse
    private lateinit var viewModel: VideoSelectionViewModel
    private lateinit var viewModelHome: HomeViewModel
    private var selectedLang: String = ""
    private var postId: String = ""
    private var firstName: String = ""

    private var handler = Handler()
    private var isApiCalled = false
    private var currentTime = 0L
    private var apiResponseTime = 0L
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var deviceManufacture: String = ""
    private var model: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(VideoSelectionViewModel::class.java)

        /* val `is`: InputStream = resources!!.openRawResource(R.raw.thumb)
         val movie = Movie.decodeStream(`is`)
         val duration = movie.duration()
         Logger.print("onPostExecute genGIF !!!!!!!!!!${duration}")
 */
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

        val density = resources.displayMetrics.density
        Logger.print("density !!!!!!!!!!!!$density")
        loginVIewModel = ViewModelProviders.of(this).get(LoginVIewModel::class.java)
        viewModelHome = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        deviceManufacture = Build.MANUFACTURER
        model = Build.MODEL

        Logger.print("device model=========$model")

        if (!deviceId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.DEVICE_ID, deviceId)
        }
        if (!deviceManufacture.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this)
                .setValue(Constants.MANUFACTURER, deviceManufacture)

        }
        if (!model.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.DEVICE_MODEL, model)

        }
        firstName = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.FIRST_NAME_PREF, "")!!

        val currentLanguage: String = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        if (currentLanguage.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.LANGUAGE_CODE_PREF, "EN")
            SharedPreferenceUtils.getInstance(this).setValue(Constants.LANGUAGE_APP_ID, "English")
        }
        //  var generateKey = generateSSHKey(this)

        Logger.print("video file is currupted=============${Methods.filePathFromDir(Constants.COMPRESSEDFILES_PATH)}")
        Logger.print(
            "video file is currupted or not=============${videoFileIsCorrupted(
                Methods.filePathFromDir(
                    Constants.COMPRESSEDFILES_PATH
                )!!
            )}"
        )

        val myVersion = Build.VERSION.RELEASE
        Logger.print("myVersion==============$myVersion")
        SharedPreferenceUtils.getInstance(this).setValue(Constants.OS_VERSION, myVersion)

        if (!currentLanguage.contentEquals("")) {
            val myLocale = Locale(currentLanguage)
            val res: Resources = resources
            val dm: DisplayMetrics = res.displayMetrics
            val conf: Configuration = res.configuration
            conf.setLocale(myLocale)
            res.updateConfiguration(conf, dm)
        }
        uniqueIdPref = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.U_ID, "")!!

        Logger.print("uniqueIdPref=========================$uniqueIdPref")
        if (uniqueIdPref.contentEquals("")) {
            uniqueId = UUID.randomUUID().toString()
        }
        if (!uniqueId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.U_ID, uniqueId)
        }

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        isLogin = SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)

        languagePref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!
        categoryPref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.CATEGORY_PREF, "")!!

        Logger.print("LANGUAGE_PREF===========================$languagePref")
        Logger.print("CATEGORY_PREF===========================$categoryPref")

        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener(this) {
            val deepLink: Uri?
            if (it != null) {
                deepLink = it.link
                Logger.print("SplashActivity > Link ${deepLink!!.path}")
                if (deepLink.getQueryParameter("postId") != null) {
                    postId = deepLink.getQueryParameter("postId")!!
                    Logger.print("SplashActivity > PostId $postId")

                }
            } else {
                Logger.print("SplashActivity > IT is null")
            }
            if (!isApiCalled) {
                isApiCalled = true
                if (isLogin) {
                    if (firstName.contentEquals("")) {
                        val intent = Intent(this, NewSignUpActivity::class.java)
                        intent.putExtra("screenName", "")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        val intentToSignUp = Intent(this, HomeActivity::class.java)
                        intentToSignUp.putExtra("screenName", "")
                        intentToSignUp.putExtra("postId", postId)
                        startActivity(intentToSignUp)
                        finish()
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }

                } else {
                    currentTime = System.currentTimeMillis()
                    userProceedAsGuest()
                }
            }
        }.addOnFailureListener(this) {
            it.printStackTrace()
            if (!isApiCalled) {
                isApiCalled = true
                if (isLogin) {
                    if (firstName.contentEquals("")) {
                        val intent = Intent(this, NewSignUpActivity::class.java)
                        intent.putExtra("screenName", "")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        val intentToSignUp = Intent(this, HomeActivity::class.java)
                        intentToSignUp.putExtra("screenName", "")
                        intentToSignUp.putExtra("postId", postId)
                        startActivity(intentToSignUp)
                        finish()
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                } else {
                    userProceedAsGuest()
                }
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }


/*
    fun generateSSHKey(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.getEncoder().encode(md.digest()))
                Log.i("AppLog", "$hashKey")
            }
        } catch (e: Exception) {
            Log.e("AppLog", "error:", e)
        }

    }
*/


    private fun videoFileIsCorrupted(path: String): Boolean {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, Uri.parse(path))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
        val hasVideo: String = ""
        var fileDuration: String = ""

        try {
            //    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)

        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            //Logger.print("duration!!!!!!!!!!!!!!${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)}")
            fileDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (fileDuration == null || fileDuration.contentEquals("null") || fileDuration.toInt() < 0) {
                Logger.print("call currupted!!!!!!!!!!!!!!")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        retriever.close()
        return "yes" == hasVideo
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

    private val runnable = Runnable {
        if (!isApiCalled) {
            isApiCalled = true
            if (isLogin) {
                if (firstName.contentEquals("")) {
                    val intent = Intent(this, NewSignUpActivity::class.java)
                    intent.putExtra("screenName", "")
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else {
                    val intentToSignUp = Intent(this, HomeActivity::class.java)
                    intentToSignUp.putExtra("screenName", "")
                    intentToSignUp.putExtra("postId", postId)
                    startActivity(intentToSignUp)
                    finish()
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            } else {
                userProceedAsGuest()
            }
        }
    }

    private fun updateUserVideoLanguage(finalCatIds: String) {
        val hashMap = HashMap<String, String>()
        hashMap["language_list"] = finalCatIds
        if (!categoryPref.contentEquals("")) {
            hashMap["category_list"] = categoryPref
        }
        loginVIewModel.getUpdatedUserLanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("updateUserVideoLanguage=======================${it.toString()}")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val languageUpdated: String = jsonObj1.getString("language")

                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_PREF, languageUpdated)
                        selectedLang = SharedPreferenceUtils.getInstance(this)
                            .getStringValue(Constants.LANGUAGE_PREF, "")!!

                        Logger.print("categoryPref in updateUserVideoLanguage ===========$categoryPref")
                        if (!categoryPref.contentEquals("")) {
                            Logger.print("categoryPref in updateUserVideoLanguage HomeActivity ===========$categoryPref")

                            val intentToSignUp = Intent(this, HomeActivity::class.java)
                            intentToSignUp.putExtra("screenName", "")
                            intentToSignUp.putExtra("postId", postId)
                            startActivity(intentToSignUp)
                            finish()
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        } else {
                            /*  val intent = Intent(this, ChooseYourCategoryIntrestActivity::class.java)
                              intent.putExtra("is_visible", "1")
                              intent.putExtra("is_show", false)
                              intent.putExtra("postId", postId)
                              startActivity(intent)
                              finish()*/
                        }
                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    private fun userProceedAsGuest() {
        val uID = SharedPreferenceUtils.getInstance(this).getStringValue(Constants.U_ID, "")
        val hashMap = HashMap<String, String>()
        hashMap["user_guid"] = uID.toString()
        loginVIewModel.userProceedAsGuest(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("userProceedAsGuest==================${it.toString()}")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        apiResponseTime = System.currentTimeMillis()
                        Utils.deleteProfileDataFile(this)
                        if (jsonObject.has("data")) {
                            userLoginResponse =
                                Gson().fromJson(
                                    jsonObject.getString("data").toString(),
                                    object : TypeToken<UserLoginResponse>() {}.type
                                )

                            if (userLoginResponse.getemail_id() != null)
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.EMAILPREF,
                                    userLoginResponse.getemail_id()!!
                                )
                            if (userLoginResponse.getcategory_list() != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.CATEGORY_PREF,
                                    userLoginResponse.getcategory_list()!!
                                )
                            }
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
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWER_COUNT_PREF,
                                    userLoginResponse.getno_of_followers()!!
                                )
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.AUTH_TOKEN_PREF,
                                jsonObject.getString("auth_token")
                            )

                            if (!languagePref.contentEquals("")) {
                                updateUserVideoLanguage(languagePref)
                            } else {
                                when {
                                    isLogin -> {
                                        if (firstName.contentEquals("")) {
                                            val intent = Intent(this, NewSignUpActivity::class.java)
                                            intent.putExtra("screenName", "")
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            val intentToSignUp =
                                                Intent(this, HomeActivity::class.java)
                                            intentToSignUp.putExtra("screenName", "")
                                            intentToSignUp.putExtra("postId", postId)
                                            startActivity(intentToSignUp)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.fade_in,
                                                R.anim.fade_out
                                            )
                                        }
                                    }
                                    languagePref.contentEquals("") -> {
                                        val intent =
                                            Intent(this, ChooseYourLanguageIntrest::class.java)
                                        intent.putExtra("is_show", false)
                                        intent.putExtra("is_visible", "1")
                                        intent.putExtra("screen_name", "")
                                        intent.putExtra("shareUri", "")
                                        intent.putExtra("postId", postId)
                                        startActivity(intent)
                                        finish()
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                    }

                                    /* categoryPref.contentEquals("") -> {
                                         val intent = Intent(this, ChooseYourCategoryIntrestActivity::class.java)
                                         intent.putExtra("is_visible", "1")
                                         intent.putExtra("is_show", false)
                                         intent.putExtra("postId", postId)
                                         startActivity(intent)
                                         finish()
                                     }*/
                                    else -> {
                                        val intentToSignUp = Intent(this, HomeActivity::class.java)
                                        intentToSignUp.putExtra("screenName", "")
                                        intentToSignUp.putExtra("postId", postId)
                                        startActivity(intentToSignUp)
                                        finish()
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                    }
                                }
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
}