package smylee.app.startPost

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_start_new_post.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.CategoryResponse
import smylee.app.model.HashTagResponse
import smylee.app.model.Videolanguageresponse
import smylee.app.selectcategory.SelectCategoryViewModel
import smylee.app.services.VideoUploadService
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.ui.Activity.PreviewActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap


class StartNewPostActivity : BaseActivity() {
    private lateinit var viewModel: SelectCategoryViewModel
    private var list = ArrayList<CategoryResponse>()
    private var listNameCat = ArrayList<String>()

    //    private var listNameCatHashTag = ArrayList<String>()
    private var hashTagList = ArrayList<HashTagResponse>()
    private var languageList = ArrayList<Videolanguageresponse>()

    //    var startPostCategoryAdapter: StartPostCategoryAdapter? = null
    private var startPostRecyclerViewAdapter: StartPostRecyclerViewAdapter? = null
    private var startPostLanguageAdapter: StartPostLanguageAdapter? = null
    private var llm: GridLayoutManager? = null
    var str1 = ""
    var isFbPost: Boolean = false
    var isInstaPost: Boolean = false
    private var callbackManager: CallbackManager? = null
    val FB_BASIC_PERMISSIONS =
        arrayOf("publish_actions")

//    var isFirst: Boolean = true

    //    private var llmLinear: LinearLayoutManager? = null
    var isLogin: Boolean = false
    var selectedLanguageId: String = ""
    var selectedCategoryId: String = ""

    //    var audioFileNameFinal :String =""
    var language = arrayOf("Cricket", "FootBall", "Chess", "Tenis")
    private var hashTagNamesList = ArrayList<String>()

    var isAPICall: Boolean = true
    private var adapter1: ArrayAdapter<String>? = null
    var searchText = ""
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //    var ISAUDIOMIX :Boolean =false
    private var isFromPickGallery: Boolean = false

    var hashCharIndex = -1
    var hashSearchText = ""

    //    private var isDownloadFinal: String = ""
    private var audioFileNameFinal: String = ""
    private var ISAUDIOMIX: Boolean = false
    private var FINALaUDIOiD: String = ""
    private var IS_MERGE: Boolean = false
    private var mStopVideo: Boolean = false
    private var videoRotation: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_new_post)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        viewModel = ViewModelProviders.of(this).get(SelectCategoryViewModel::class.java)
        ll_close.setOnClickListener {
            onBackPressed()
        }

        if (intent != null) {
            isFromPickGallery = intent.getBooleanExtra("isFromPickGallery", false)
            audioFileNameFinal = intent.getStringExtra("audioFileNameFinal")!!
            Logger.print("audioFileNameFinal!!!!!!!!!!!!!!!!!!!!!!!!!!" + audioFileNameFinal)
            if (audioFileNameFinal != "") {
                ISAUDIOMIX = true
            }
            FINALaUDIOiD = intent.getStringExtra("finalAudioId")!!
            IS_MERGE = intent.getBooleanExtra("IS_MERGE_FILE", false)
            mStopVideo = intent.getBooleanExtra("mStopVideo", false)
            videoRotation = intent.getIntExtra("videoRotation", 0)
        }

        ivClosePost.setOnClickListener {
            //onBackPressed()
            /*val intent = Intent()
            intent.putExtra("postTitle",auto_post.text.toString().trim())
            intent.putExtra("selectedLanguageId", selectedLanguageId)
            intent.putExtra("selectedcategoryId", selectedCategoryId)
            intent.putExtra("isFromPickGallery", isFromPickGallery)
            setResult(Activity.RESULT_CANCELED, intent)*/
            finish()
            //finish()
        }

        ivBackPost.setOnClickListener {
//            val intent = Intent()
//            intent.putExtra("postTitle",auto_post.text.toString().trim())
//            intent.putExtra("selectedLanguageId", selectedLanguageId)
//            intent.putExtra("selectedcategoryId", selectedCategoryId)
//            intent.putExtra("isFromPickGallery", isFromPickGallery)
//            setResult(Activity.RESULT_CANCELED, intent)
            onBackPressed()
            finish()
        }

        isLogin = SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)

        //auto_post.threshold = 300
        // getHashTagList(auto_post.text.toString())

        Utils.hideKeyboard(this)
        getCategorySelection()
        getVideoSelectionLanguage()
        auto_post.setTokenizer(SpaceTokenizer())

        auto_post.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        auto_post.text = auto_post.text.append(" ")
                        auto_post.setSelection(auto_post.text.length)
                        Utils.hideKeyboard(this@StartNewPostActivity)
                        // the user is done typing.
                        return true // consume.
                    }
                }
                return false // pass on to other listeners.
            }
        }
        )


/*
        FacebookSdk.sdkInitialize(applicationContext)
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
        }
*/
        //callbackManager = CallbackManager.Factory.create()

        /*    swFb?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    isFbPost = true
                    doLogin(FB_BASIC_PERMISSIONS)

                } else {
                    isFbPost = false
                }
            }
            sbInsta?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    isInstaPost = true
                    doLogin(FB_BASIC_PERMISSIONS)

                } else {
                    isInstaPost = false

                }
            }
    */
        /*LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(result: LoginResult?) {

                    Logger.print("result onSuccess=========${result.toString()}")
                    val accessToken = AccessToken.getCurrentAccessToken()
                    Logger.print("accessToken=========${AccessToken.getCurrentAccessToken().token}")
                    Logger.print("LoginResult AccessToken=========${result?.accessToken!!.token}")
                    val fbSharePath = Methods.filePathFromDir(Constants.DOWNLOADED_PATH)
                    Logger.print("fbSharePath=============$fbSharePath")
                    if (AccessToken.getCurrentAccessToken().token == null) {
                        swFb?.isChecked = false
                    }
                    //   shareVideoFB(fbSharePath!!, result.accessToken?.token!!)
                    //getFacebookPageID()
                }

                override fun onCancel() {
                    Logger.print("result onCancel=========")

                }

                override fun onError(error: FacebookException?) {
                    Logger.print("result onError=========${error.toString()}")

                }

            })
        */


        auto_post.addTextChangedListener(object : TextWatcher {


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s!!.toString()
                if (searchText.lastIndex != -1) {
                    if (searchText[searchText.lastIndex] == '#') {
                        hashCharIndex = searchText.lastIndex
                        hashSearchText = "#"
                    } else if (searchText[searchText.lastIndex] == ' ') {
                        hashCharIndex = -1
                        hashSearchText = ""
                    }
                }

                /*if(searchText[searchText.lastIndex] == '#') {
                    hashCharIndex = searchText.lastIndex
                    hashSearchText = "#"
                } else if (searchText[searchText.lastIndex] == ' ') {
                    hashCharIndex = -1
                    hashSearchText = ""
                }*/

                if (hashCharIndex > -1 && hashCharIndex <= searchText.lastIndex) {
                    hashSearchText = searchText.substring(hashCharIndex, searchText.length)
                }

                //if (searchText.length>0 && searchText.startsWith("#"))
                /*if (searchText.isNotEmpty() && searchText.contains("#")) {
                    if (searchText.contains(" ")) {
                        searchText ="#"
                    }
                }*/

                if (hashSearchText.startsWith("#") && isAPICall) {
                    Logger.print("onTextChanged===========================$searchText")
                    getHashTagList(hashSearchText)
                } else if (!isAPICall) {
                    isAPICall = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {
            }
        })

        btnCreatePost.setOnClickListener {
            btnCreatePost.isEnabled = false

            Handler().postDelayed({ // This method will be executed once the timer is over
                btnCreatePost.isEnabled = true
            }, 2000)


            if (auto_post.text.toString().trim().contentEquals("") || auto_post.text.isEmpty()) {
                Utils.showToastMessage(context = this, message = getString(R.string.post_title))
            } else if (selectedCategoryId.contentEquals("")) {
                Utils.showToastMessage(
                    context = this,
                    message = getString(R.string.select_category)
                )
            } else if (selectedLanguageId.contentEquals("")) {
                Utils.showToastMessage(
                    context = this,
                    message = getString(R.string.select_language)
                )
            } else {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selectedLanguageId)
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selectedCategoryId)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

                /*val intent = Intent()
                intent.putExtra("postTitle",auto_post.text.toString().trim())
                intent.putExtra("selectedLanguageId", selectedLanguageId)
                intent.putExtra("selectedcategoryId", selectedCategoryId)
                intent.putExtra("isFromPickGallery", isFromPickGallery)
                setResult(Activity.RESULT_OK, intent)
                finish()*/
                showDialogForPostVideo(isFromPickGallery)
            }
        }
    }

    private fun showDialogForPostVideo(isFromPickGallery: Boolean) {
        val inflater = LayoutInflater.from(this)

        val dialogView: View = inflater.inflate(R.layout.post_video_layout, null)
        val builder = AlertDialog.Builder(this)

        val b = builder.create()
        builder.setCancelable(true)
        builder.setView(dialogView)
        b.setCanceledOnTouchOutside(true)

        val show = builder.show()
        show.setCancelable(true)
        show.setCanceledOnTouchOutside(true)

        val tvYes = dialogView.findViewById<View>(R.id.tv_yes) as TextView
        val tvNo = dialogView.findViewById<View>(R.id.tv_no) as TextView

        show.setOnDismissListener {
            setResult(Activity.RESULT_OK)
            this@StartNewPostActivity.finish()
            if (PreviewActivity.previewActivity != null) {
                PreviewActivity.previewActivity!!.finish()
            }
            if (CameraVideoRecording.cameraVideoRecording != null) {
                CameraVideoRecording.cameraVideoRecording!!.setResult(Activity.RESULT_OK)
                CameraVideoRecording.cameraVideoRecording!!.finish()
            }
            Log.i("CameraVideoRecording", "onDismiss Popup")
        }


        tvNo.setOnClickListener {
            show.dismiss()
        }

        tvYes.setOnClickListener {
            val serviceIntent = Intent(this, VideoUploadService::class.java)
            Logger.print("ISAUDIOMIX upload service=========$ISAUDIOMIX")
            serviceIntent.putExtra("audioFileName", audioFileNameFinal)
            serviceIntent.putExtra("finalAudioId", FINALaUDIOiD)
            serviceIntent.putExtra("isAudioMix", ISAUDIOMIX)
            serviceIntent.putExtra("IS_MERGE_FILE", IS_MERGE)
            serviceIntent.putExtra("mStopVideo", mStopVideo)
            serviceIntent.putExtra("selectedCatIds", selectedCategoryId)
            serviceIntent.putExtra("selectedLanguageIds", selectedLanguageId)
            serviceIntent.putExtra("postTitle", auto_post.text.toString().trim())
            serviceIntent.putExtra("videoRotation", videoRotation)
            serviceIntent.putExtra("isFromPickGallery", isFromPickGallery)
            serviceIntent.putExtra("isFbPost", isFbPost)
            serviceIntent.putExtra("isInstaPost", isInstaPost)
            ContextCompat.startForegroundService(this, serviceIntent)
            show.dismiss()
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("postTitle", auto_post.text.toString().trim())
        intent.putExtra("selectedLanguageId", selectedLanguageId)
        intent.putExtra("selectedcategoryId", selectedCategoryId)
        intent.putExtra("isFromPickGallery", isFromPickGallery)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

/*
    open fun doLogin() {
        doLogin(FB_BASIC_PERMISSIONS)
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //   callbackManager?.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getFacebookPageID() {
        val request = GraphRequest.newGraphPathRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/accounts",
            object : GraphRequest.Callback {
                override fun onCompleted(response: GraphResponse?) {
                    Logger.print("response: onCompleted =============$response")
                }
            })

        val parameters = Bundle()
        parameters.putString(
            "access_token",
            AccessToken.getCurrentAccessToken().token
        )
        request.parameters = parameters
        request.executeAsync()
    }

    open fun doLogin(permissions: Array<String>) {
        try {
            LoginManager.getInstance().logOut()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        //  LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"))
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        //   LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
    }

    private fun getCategorySelection() {
        val hashMap = HashMap<String, String>()
//        val apiName: String = APIConstants.USERCATEGORY
        viewModel.getCategoryForUser(this, hashMap)
            .observe(this, Observer {
                if (it != null) {
                    Logger.print("getCategorySelection start postResponse : $it")
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")

                    if (code == 1) {
                        list.clear()
                        listNameCat.clear()
                        list = Gson().fromJson(
                            jsonObject.getJSONArray("data").toString(),
                            object : TypeToken<ArrayList<CategoryResponse>>() {}.type
                        )
                        if (list.size > 0) {
                            for (i in list.indices) {
                                listNameCat.add(list[i].category_name!!)
                            }
                            gvCategory.visibility = View.VISIBLE
                            tvCategory.visibility = View.VISIBLE
                            btnCreatePost.visibility = View.VISIBLE
                            // llShareInsta.visibility = View.VISIBLE
                            //  llShareFb.visibility = View.VISIBLE
                            setAdapter()
                        } else {
                            gvCategory.visibility = View.GONE
                            tvCategory.visibility = View.GONE
                        }
                    } else {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                }
            })
    }

    private fun getHashTagList(searchText: String) {
        val hashMap = HashMap<String, String>()
        hashMap["search_text"] = searchText
//        val apiName: String = APIConstants.HASHTAGLIST
        viewModel.getHashTagList(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("HASHTAGLIST response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    hashTagNamesList.clear()
                    hashTagList.clear()
                    hashTagList = Gson().fromJson(
                        jsonObject.getJSONArray("data").toString(),
                        object : TypeToken<ArrayList<HashTagResponse>>() {}.type
                    )

                    if (hashTagList.size > 0) {
                        Logger.run { print("hashTagList!!!!!!!!!!!!!!!!!!!!!$hashTagList") }
                        for (i in hashTagList.indices) {
                            hashTagNamesList
                            hashTagNamesList.add(hashTagList[i].tag_name.toString())
                        }
                        Logger.print("hashtagNamesList!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$hashTagNamesList")
                        adapter1 = ArrayAdapter(this, R.layout.spinner_item, hashTagNamesList)
                        auto_post.setAdapter(adapter1)
                        auto_post.threshold = 1
                        auto_post.showDropDown()
                        adapter1!!.notifyDataSetChanged()

                        auto_post.setOnItemClickListener { arg0, arg1, arg2, arg3 ->
                            Logger.print("hashtagNamesList[arg2].toString()!!!!!!!!!!!!!!!!!" + hashTagNamesList[arg2])
                            auto_post.clearFocus()
                            auto_post.setAdapter(null)
//                            isAPICall=false
                        }
                    } else if (code == 0) {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                } else {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    private fun getVideoSelectionLanguage() {
        val hashMap = HashMap<String, String>()
//        val apiName: String = APIConstants.VIDEOLANGUAGE
        viewModel.getVideoSelectionlanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("getVideoSelectionLanguage Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    //Utils.showToastMessage(this, jsonObject["message"].toString())
                    languageList.clear()
                    selectedLangId.clear()
                    selectedLang.clear()
                    languageList = Gson().fromJson(
                        jsonObject.getJSONArray("data").toString(),
                        object : TypeToken<ArrayList<Videolanguageresponse>>() {}.type
                    )
                    if (languageList.size > 0) {
                        tvLanguage.visibility = View.VISIBLE
                        gvLanguage.visibility = View.VISIBLE

                        if (btnCreatePost.visibility == View.GONE) {
                            btnCreatePost.visibility = View.VISIBLE
                        }
                        for (i in languageList.indices) {
                            if (languageList[i].is_selected == 1) {
                                selectedLangId.add(languageList[i].language_id.toString())
                                selectedLang.add(languageList[i].language_name.toString())
                            }
                        }
                        /*Logger.print("selectedLangId size$$$$$$$=====${selectedLangId.size}")

                        if (selectedLangId.size == 0) {

                            for (i in languageList.indices) {
                                selectedLangId.add(languageList[i].language_id.toString())
                                selectedLang.add(languageList[i].language_name.toString())
                            }
                        }*/
                        setLanguageAdapter()
                    } else {
                        tvLanguage.visibility = View.GONE
                        gvLanguage.visibility = View.GONE
                    }
                } else {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }

    private fun setLanguageAdapter() {
        if (startPostLanguageAdapter == null) {
            llm = GridLayoutManager(this, 3)
            gvLanguage.layoutManager = llm
            startPostLanguageAdapter = StartPostLanguageAdapter(
                this,
                languageList,
                selectedLangId,
                selectedLang,
                object : StartPostLanguageAdapter.ManageClick {
                    override fun manageClick(catId: String?) {
                        if (catId != null && catId != "") {
                            selectedLanguageId = catId
                        }
                        Logger.print("CLICK LANGUAGE ID===========$catId")
                    }
                })
            gvLanguage.adapter = startPostLanguageAdapter
        } else {
            startPostLanguageAdapter?.notifyDataSetChanged()
        }
    }

    private fun setAdapter() {
        if (startPostRecyclerViewAdapter == null) {
            llm = GridLayoutManager(this, 3)
            gvCategory.layoutManager = llm
            startPostRecyclerViewAdapter = StartPostRecyclerViewAdapter(
                this,
                list,
                object : StartPostRecyclerViewAdapter.ManageClick {
                    override fun manageClick(catId: String?, cat_name: String?) {
                        if (catId != null && catId != "") {
                            selectedCategoryId = catId
                            if (auto_post.text == null || auto_post.text.toString()
                                    .contentEquals("")
                            ) {
                                if (cat_name != "") {

                                    auto_post.clearFocus()
                                    auto_post.setAdapter(null)
                                    isAPICall = false
                                    auto_post.setText("#$cat_name ")
                                    auto_post.setSelection(cat_name!!.length)
                                    isAPICall = true
                                    str1 = auto_post.text.toString()

                                }
                            } else {
                                var str = auto_post.text.toString()
                                Logger.print("hash tag !!!!!!!!!!!!!!!!!!!!!!!!" + str)
                                if (listNameCat.size > 0) {
                                    if (str1.contains("#")) {
                                        str1 = str1.replace("#", "")

                                    }

                                    if (str1.contains(" ")) {
                                        str1 = str1.replace(" ", "")

                                    }
                                    Logger.print("str1!!!!!!!!!!!!!!!!!!!!!!!!" + str1)

                                    if (listNameCat.contains(str1)) {
                                        auto_post.clearFocus()
                                        auto_post.setAdapter(null)
                                        isAPICall = false

                                        Logger.print("contains @@@@@@@@@@@@@@@@@@")
                                        auto_post.setText("")
                                        auto_post.setText(str + "#$cat_name ")
                                        //  auto_post.setText("#$cat_name ")
                                        auto_post.setSelection(cat_name!!.length)
                                        isAPICall = true
                                    }
                                }
                            }
                        }
                        Logger.print("CLICK CAT ID===========$catId")
                    }
                })
            gvCategory.adapter = startPostRecyclerViewAdapter
        } else {
            startPostRecyclerViewAdapter?.notifyDataSetChanged()
        }
    }

    companion object {
        var selectedLangId = ArrayList<String>()
        var selectedLang = ArrayList<String>()
    }
}