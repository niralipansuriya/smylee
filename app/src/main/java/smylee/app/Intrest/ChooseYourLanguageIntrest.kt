package smylee.app.Intrest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_choose_your_language_intrest.*
import kotlinx.android.synthetic.main.activity_video_selection_language.tv_nodata
import org.json.JSONObject
import smylee.app.R
import smylee.app.VideoLanguageSelection.VideoSelectionViewModel
import smylee.app.home.HomeActivity
import smylee.app.login.LoginActivity
import smylee.app.login.LoginVIewModel
import smylee.app.model.UserLoginResponse
import smylee.app.model.Videolanguageresponse
import smylee.app.ui.Activity.PreviewActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChooseYourLanguageIntrest : BaseActivity() {
    private lateinit var viewModel: VideoSelectionViewModel
    private var list = ArrayList<Videolanguageresponse>()
    private var interestLanguageAdapter: IntrestLanguageAdapter? = null
    private var isShow: Boolean = false
    private var llm: GridLayoutManager? = null
    private var selectedLang: String = ""
    private var isVisible: String = ""
    private var postId: String = ""
    private var screenName: String = ""
    private var shareUri: Uri? = null
    private lateinit var loginVIewModel: LoginVIewModel
    private var apiResponseTime = 0L
    private lateinit var userLoginResponse: UserLoginResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_language_intrest)
        viewModel = ViewModelProviders.of(this).get(VideoSelectionViewModel::class.java)
        loginVIewModel = ViewModelProviders.of(this).get(LoginVIewModel::class.java)

        intrestLanguageId.clear()
        intrestLanguageName.clear()
        unSubscribeTopicList.clear()

        if (SharedPreferenceUtils.getInstance(this).getStringValue(
                Constants.AUTH_TOKEN_PREF, ""
            ) != ""
        ) {
            getVideoSelectionLanguage()

        }

        if (intent != null) {
            isShow = intent.getBooleanExtra("is_show", false)
            if (intent.hasExtra("postId")) {
                postId = intent.getStringExtra("postId")!!
            }

            if (intent.hasExtra("screen_name")) {
                screenName = intent.getStringExtra("screen_name")!!
                shareUri = intent.getParcelableExtra("shareUri")

                if (screenName.contentEquals("preview")) {
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this)
                        .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
                    if (currentLanguage.contentEquals("")) {
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_CODE_PREF, "EN")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_APP_ID, "English")
                    }

                    if (SharedPreferenceUtils.getInstance(this)
                            .getStringValue(Constants.U_ID, "") == ""
                    ) {
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.U_ID, UUID.randomUUID().toString())
                    }

                    userProceedAsGuest()
                }

                Logger.print("screenName===========$screenName")
                Logger.print("shareUri===========$shareUri")
            }

            Logger.print("is_show=================$isShow")
        }
        if (isShow) {
            btn_submit.text = resources.getString(R.string.submit)
        }

        if (intent.getStringExtra("is_visible") != null && !intent.getStringExtra("is_visible")!!.contentEquals("")) {
            isVisible = intent.getStringExtra("is_visible")!!
            Logger.print("is_visible===$isVisible")
        }

        val spacing = 40 // 50px
        val includeEdge = false
        rvIntrestLanguage.addItemDecoration(GridSpacingItemDecoration(2, spacing, includeEdge))

        btn_submit.setOnClickListener {
            Log.d("ADDED ID LANGUAGE", intrestLanguageId.toString())
            if (intrestLanguageId.size <= 0) {
                Utils.showAlert(this, "", resources.getString(R.string.select_language))
            } else {
                var finalCatIds: String
                finalCatIds = intrestLanguageId.toString()
                if (finalCatIds.contains("[")) {
                    finalCatIds = finalCatIds.replace("[", "")
                }
                if (finalCatIds.contains("]")) {
                    finalCatIds = finalCatIds.replace("]", "")
                }
                Logger.print("FINAL LANGUAGE ID", finalCatIds)
                updateUserVideoLanguage(finalCatIds)
            }
        }
    }

    private fun getVideoSelectionLanguage() {
        val hashMap = HashMap<String, String>()
        viewModel.getVideoSelectionLanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("getVideoSelectionLanguage Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    list.clear()
                    list = Gson().fromJson(jsonObject.getJSONArray("data").toString(),
                        object : TypeToken<ArrayList<Videolanguageresponse>>() {}.type)
                    if (list.size > 0) {
                        tv_nodata.visibility = View.GONE
                        rvIntrestLanguage.visibility = View.VISIBLE
                        btn_submit.visibility = View.VISIBLE
                        setAdapter()
                        if (isShow) {
                            for (i in list.indices) {
                                if (list[i].is_selected == 1) {
                                    intrestLanguageId.add(list[i].language_id.toString())
                                    intrestLanguageName.add(list[i].language_name.toString())
                                }
                            }
                        } else if (screenName.contentEquals("preview")) {
                            intrestLanguageId.add("11")
                            intrestLanguageId.add("1")
                        } else {
                            intrestLanguageId.add("11")
                            intrestLanguageId.add("1")
                        }
                    } else {
                        tv_nodata.visibility = View.VISIBLE
                        rvIntrestLanguage.visibility = View.GONE
                        btn_submit.visibility = View.GONE
                    }
                } else {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    private fun setAdapter() {
        if (interestLanguageAdapter == null) {
            llm = GridLayoutManager(this, 2)
            rvIntrestLanguage.layoutManager = llm
            interestLanguageAdapter =
                IntrestLanguageAdapter(this, list, object : IntrestLanguageAdapter.ManageClick {
                    override fun manageClick(catId: String?) {
                    }
                })
            rvIntrestLanguage.adapter = interestLanguageAdapter
        } else {
            interestLanguageAdapter?.notifyDataSetChanged()
        }
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
                            getVideoSelectionLanguage()

                            /*   val intent = Intent(this, ChooseYourLanguageIntrest::class.java)
                               intent.putExtra("is_show", true)
                               intent.putExtra("is_visible", "0")
                               intent.putExtra("screen_name", "preview")
                               intent.putExtra("shareUri", shareUri)
                               startActivity(intent)*/
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
        viewModel.getUpdatedUserLanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("UPDATEVIDEOLANGUAGE Response : $it")
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
                        Logger.print("language_updated ======================$languageUpdated")
                    }
                    if (screenName.contentEquals("preview") && !SharedPreferenceUtils.getInstance(
                            this
                        )
                            .getBoolanValue(
                                Constants.IS_LOGIN,
                                false
                            ) && shareUri != null && shareUri!!.path != ""
                    ) {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("screen_name", "preview")
                        intent.putExtra("shareUri", shareUri)
                        startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                        finish()
                    } else if (screenName.contentEquals("preview") && SharedPreferenceUtils.getInstance(
                            this
                        )
                            .getBoolanValue(
                                Constants.IS_LOGIN,
                                false
                            ) && shareUri != null && shareUri!!.path != ""
                    ) {
                        val intent = Intent(this, PreviewActivity::class.java)
                        intent.putExtra("shareUri", shareUri)
                        intent.putExtra("IS_AUDIO_CAMERA", false)
                        intent.putExtra("IS_MERGE", false)
                        intent.putExtra("IS_CAMERA_PREVIEW", false)
                        intent.putExtra("extractAudio", false)
                        intent.putExtra("videoRotation", false)
                        intent.putExtra("audioFile", "")
                        startActivity(intent)
                    } else if (isVisible.contentEquals("0")) {
                        onBackPressed()
                    } else {
                        /*  val intent = Intent(this, ChooseYourCategoryIntrestActivity::class.java)
                          intent.putExtra("is_visible", "1")
                          intent.putExtra("is_show", false)
                          intent.putExtra("postId", postId)
                          startActivity(intent)
                          finish()  */

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("screenName", "")
                        intent.putExtra("postId", postId)
                        startActivity(intent)
                        finish()
                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    companion object {
        var intrestLanguageId = java.util.ArrayList<String>()
        var intrestLanguageName = java.util.ArrayList<String>()
        var unSubscribeTopicList = java.util.ArrayList<String>()
    }
}