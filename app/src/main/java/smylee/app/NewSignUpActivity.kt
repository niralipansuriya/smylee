package smylee.app

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ibotta.android.support.pickerdialogs.SupportedDatePickerDialog
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_new_sign_up.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import smylee.app.Profile.ProfileViewModel
import smylee.app.dialog.CommonAlertDialog
import smylee.app.home.HomeActivity
import smylee.app.login.LoginVIewModel
import smylee.app.model.UpdateProfileResponse
import smylee.app.model.UserLoginResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class NewSignUpActivity : BaseActivity(), CameraUtils.OnCameraResult,
    SupportedDatePickerDialog.OnDateSetListener {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var userProfileResponse: UpdateProfileResponse
    private var photoFile: File? = null
    private lateinit var userLoginResponse: UserLoginResponse
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    var hashMap = HashMap<String, RequestBody>()
    private var cameraUtils: CameraUtils? = null
   // private var datePickerFragment: DatePickerFragment? = null
    private lateinit var loginVIewModel: LoginVIewModel

    private var screenName: String = ""
    private var myCalendar: Calendar? = null
    private var myCalendar2: GregorianCalendar? = null
    private var genderTypeStr: String = "" //M: Male F:Female B:Both

    private var languagePref: String = ""
    private var categoryPref: String = ""

    /*added for lib*/
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private val dateTime = Calendar.getInstance()
    private val allowedDOB: Int = 13

    private var deviceManufacturer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_sign_up)
        loginVIewModel = ViewModelProviders.of(this).get(LoginVIewModel::class.java)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        Utils.hideKeyboard(this)
        languagePref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.LANGUAGE_PREF, "")!!
        categoryPref =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.CATEGORY_PREF, "")!!

        myCalendar = Calendar.getInstance()
        myCalendar2 = GregorianCalendar()
        if (intent != null) {
            screenName = intent.getStringExtra("screenName")!!
        }
        cameraUtils = CameraUtils(this, this)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        deviceManufacturer =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.MANUFACTURER, "")!!
        edt_bod.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showSpinnerDatePickerDialog()
            }
            false
        })

        /*edt_bod.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                datePickerFragment = DatePickerFragment(edt_bod)
                datePickerFragment!!.show(supportFragmentManager, "Select Your Birthday")
                return@OnTouchListener true
            }
            false
        })
*/

        iv_edit.setOnClickListener {
            cameraUtils?.openCameraGallery()
        }
        /* if (!categoryPref.contentEquals("")) {
             getUpdatedUserCategorySelection(categoryPref)
         }
         if (!languagePref.contentEquals("")) {
             updateUserVideoLanguage(languagePref)
         }*/

        rb_male_profile.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "1"
                rb_male_profile.setTextColor(ContextCompat.getColor(this, R.color.purple))
                rb_female_profile.setTextColor(ContextCompat.getColor(this, R.color.light_purple))
            }
        }

        rb_female_profile.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "2"
                rb_female_profile.setTextColor(ContextCompat.getColor(this, R.color.purple))
                rb_male_profile.setTextColor(ContextCompat.getColor(this, R.color.light_purple))
            }
        }

        btn_submit.setOnClickListener {
            if (Utils.isTextEmpty(edt_first_name)) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.fill_first_name)
                )
            } else if (edt_email.text.toString().trim().isNotEmpty()) {
                if (!Utils.isEmailValid(edt_email.text.toString().trim())) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.fill_valid_email)
                    )
                } else if (Utils.isTextEmpty(edt_bod)) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.fill_birth_date)
                    )
                } else if (genderTypeStr.contentEquals("")) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.select_gender)
                    )
                } else {
                    registerUserData()
                }
            } else if (Utils.isTextEmpty(edt_bod)) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.fill_birth_date)
                )
            } else if (genderTypeStr.contentEquals("")) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.select_gender)
                )
            } else {
                registerUserData()
            }
        }
    }

    /*class DatePickerFragment(private var edtBirthDate: TextInputEditText) : DialogFragment(),
        DatePickerDialog.OnDateSetListener {
        private var mYear: Int = 0
        private var mMonth: Int = 0
        private var mDay: Int = 0
        private val dateTime: Calendar = Calendar.getInstance()
        private val allowedDOB: Int = 13

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val str = changeDateFormat(edtBirthDate.text.toString())

            //if (edtBirthDate.text.toString().trim() != "") {
            if (str != "") {
                if (str!!.contains("-")) {
                    val lstValues: List<String> = str.split("-")
                    Logger.print("lstValues!!!!!!!!!!$lstValues")
                    Logger.print("0th index element!!!!${lstValues[0]}")
                    Logger.print("1th index element!!!!${lstValues[1]}")
                    Logger.print("2th index element!!!!${lstValues[2]}")
                    mYear = lstValues[0].toInt()
                    mMonth = if (lstValues[1].toInt() != 0) {
                        lstValues[1].toInt() - 1
                    } else {
                        lstValues[1].toInt()
                    }
                    mDay = lstValues[2].toInt()
                }
                val datePickerDialog =
                    DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[1].performClick();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[0].performClick();
                }
                *//*    val view: View =
                        datePickerDialog.getLayoutInflater().inflate(R.layout.title_view, null)

                    datePickerDialog.setCustomTitle(view)*//*
                return datePickerDialog
                //  return DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
            } else {
                val c = Calendar.getInstance()
                mYear = c[Calendar.YEAR]
                mMonth = c[Calendar.MONTH]
                mDay = c[Calendar.DAY_OF_MONTH]
                val datePickerDialog =

                    DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
                *//*  val view: View =
                      datePickerDialog.getLayoutInflater().inflate(R.layout.title_view, null)
                  datePickerDialog.setCustomTitle(view)*//*

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[1].performClick();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[0].performClick();
                }
                return datePickerDialog
                // return DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
            }
        }

        override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            mYear = year
            mMonth = monthOfYear
            mDay = dayOfMonth
            dateTime.set(mYear, monthOfYear, dayOfMonth)
            val selectDateInMilliSeconds: Long = dateTime.timeInMillis
            val currentDate = Calendar.getInstance()
            val currentDateInMilliSeconds = currentDate.timeInMillis
            val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val strDt = simpleDate.format(dateTime.time)
            val diffDate = currentDateInMilliSeconds - selectDateInMilliSeconds
            val yourAge = Calendar.getInstance()
            yourAge.timeInMillis = diffDate
            val returnedYear = (yourAge[Calendar.YEAR] - 1970).toLong()

            when {
                selectDateInMilliSeconds > currentDateInMilliSeconds -> {
                    Toast.makeText(activity, R.string.validate_age_, Toast.LENGTH_LONG).show()
                    return
                }
                returnedYear < allowedDOB -> {
                    Toast.makeText(activity, R.string.validate_age, Toast.LENGTH_LONG).show()
                    return
                }
                else -> {
                    //edtBirthDate.setText(strDt)
                    parseDate(strDt, edtBirthDate)
                }
            }
        }
    }
*/
    override fun onBackPressed() {

        val alertDialog =
            object : CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {

                    signOutAPICall()
                }

                override fun cancelClicked() {

                }
            }
        alertDialog.initDialog(
            resources.getString(R.string.ok),
            resources.getString(R.string.signup_back_msg)
        )
        alertDialog.setCancelable(true)
        alertDialog.show()


        /*  val intent = Intent(this, HomeActivity::class.java)
          intent.putExtra("screenName", screenName)
          startActivity(intent)
          Logger.print("screen_name======================$screenName")
          finish()*/
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
                            userLoginResponse =
                                Gson().fromJson(
                                    jsonObject.getString("data").toString(),
                                    object : TypeToken<UserLoginResponse>() {}.type
                                )
                            Logger.print("userLoginResponse===============$userLoginResponse")

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
                            if (!SharedPreferenceUtils.getInstance(this)
                                    .getStringValue(Constants.LANGUAGE_PREF, "")!!.contentEquals("")
                            ) {
                                updateUserVideoLanguage(
                                    SharedPreferenceUtils.getInstance(this)
                                        .getStringValue(Constants.LANGUAGE_PREF, "")!!
                                )
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
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.CATEGORY_PREF, categoryUpdated)

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("screenName", screenName)
                        startActivity(intent)
                        Logger.print("screen_name======================$screenName")
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
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LANGUAGE_PREF, languageUpdated)

                        if (!SharedPreferenceUtils.getInstance(this)
                                .getStringValue(Constants.CATEGORY_PREF, "")!!.contentEquals("")
                        ) {
                            getUpdatedUserCategorySelection(
                                SharedPreferenceUtils.getInstance(this)
                                    .getStringValue(Constants.CATEGORY_PREF, "")!!
                            )
                        }

                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
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
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.skip_login, false)
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.AUTH_TOKEN_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.EMAILPREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.FIRST_NAME_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.LAST_NAME_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.EMAILPREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.USER_REGISTER_STATUS_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.IS_VERIFIED_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.DOB_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.FOLLOWING_COUNT_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.FOLLOWER_COUNT_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.USER_ID_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.BLOCK_USER_PREF, "")
                        SharedPreferenceUtils.getInstance(this).setValue(Constants.GENDER_PREF, 0)
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.PROFILE_PIC_PREF, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.CROPED_PROFILE_PIC, "")
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.IS_BLOCKED_PREF, 0)
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
        loginVIewModel.SendFcmToken(this, hashMap, true).observe(this, Observer {
            if (it != null) {
                Logger.print("SETUSERDEVICERELATION Response : $it")
            }
        })
    }

    private fun registerUserData() {
        val hashMap = HashMap<String, String>()
        if (edt_first_name.text.toString().isNotEmpty()) {
            hashMap["first_name"] = edt_first_name.text.toString().trim()
        }
        if (edt_lastname.text.toString().isNotEmpty()) {
            hashMap["last_name"] = edt_lastname.text.toString().trim()
        }
        if (edt_email.text.toString().isNotEmpty()) {
            hashMap["email_id"] = edt_email.text.toString().trim()
        }
        if (edt_bod.text.toString().isNotEmpty()) {
            val finalDate = changeDateFormat(edt_bod.text.toString())
            // hashMap["date_of_birth"] = edt_bod.text.toString().trim()
            hashMap["date_of_birth"] = finalDate!!.trim()
        }
        if (!genderTypeStr.contentEquals("")) {
            if (rb_female_profile.isChecked) {
                genderTypeStr = "2"
            }
            if (rb_male_profile.isChecked) {
                genderTypeStr = "1"
            }
            hashMap["gender"] = genderTypeStr
        }

        viewModel.observeUpdateProfile(this, hashMap, true).observe(this, Observer {
            if (it != null) {
                Logger.print("Register user data response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        if (jsonObject.has("data")) {
                            val data = jsonObject.getJSONObject("data")
                            userProfileResponse = Gson().fromJson(
                                data.toString(),
                                object : TypeToken<UpdateProfileResponse>() {}.type
                            )
                            if (userProfileResponse.email_id != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(Constants.EMAILPREF, userProfileResponse.email_id!!)
                            }
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.LAST_NAME_PREF, userProfileResponse.last_name)
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.FIRST_NAME_PREF, userProfileResponse.first_name)
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.GENDER_PREF, userProfileResponse.gender)
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.DOB_PREF, userProfileResponse.date_of_birth)
                            if (userProfileResponse.blocked_user_id != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.BLOCK_USER_PREF,
                                    userProfileResponse.blocked_user_id!!
                                )
                            }
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.IS_BLOCKED_PREF, userProfileResponse.is_blocked)
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.IS_VERIFIED_PREF,
                                userProfileResponse.is_verified
                            )
                            if (userProfileResponse.no_of_followers != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWER_COUNT_PREF,
                                    userProfileResponse.no_of_followers!!
                                )
                            }
                            if (userProfileResponse.no_of_followings != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWING_COUNT_PREF,
                                    userProfileResponse.no_of_followings!!
                                )
                            }

                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.PROFILE_PIC_PREF,
                                userProfileResponse.profile_pic
                            )
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.REG_ID_PREF,
                                userProfileResponse.user_register_status
                            )
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.USER_ID_PREF, userProfileResponse.user_id)

                            val bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "NewUserRegistered")
                            bundle.putString(
                                FirebaseAnalytics.Param.ITEM_ID,
                                userProfileResponse.user_id.toString()
                            )
                            firebaseAnalytics.logEvent(
                                FirebaseAnalytics.Event.SELECT_CONTENT,
                                bundle
                            )


                            if (screenName.contentEquals("profile") || screenName.contentEquals("notification") || screenName.contentEquals(
                                    "following"
                                ) || screenName.contentEquals("startPost")
                            ) {
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("screenName", screenName)
                                startActivity(intent)
                                Logger.print("screen_name======================$screenName")
                                finish()
                            }
                            if (screenName.contentEquals("player") || screenName.contentEquals("forYou") || screenName.contentEquals(
                                    "otherprofile"
                                ) || screenName.contentEquals("search") || screenName.contentEquals(
                                    "unfollow"
                                ) || screenName.contentEquals("follow")
                            ) {
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("screenName", "")
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else if (jsonObject["code"] == 2) {
                        Utils.showAlert(this, "", jsonObject["message"].toString())
                        onBackPressed()
                    } else {
                        Utils.showAlert(this, "", jsonObject["message"].toString())
                        onBackPressed()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onSuccess(images: MutableList<ChosenImage>?) {
        if (images != null && images.size > 0) {
            photoFile = File(images[0].originalPath)
            CropImage.activity(Uri.fromFile(photoFile))
                .setAllowFlipping(false)
                .setFlipHorizontally(false)
                .setFlipVertically(false)
                .setFixAspectRatio(true)
                .start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraUtils?.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) run {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri? = result.uri
                photoFile = File(resultUri!!.path!!)

                var multiBody: List<MultipartBody.Part>? = null
                if (photoFile != null) {
                    multiBody = ArrayList()
                    //  val reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile!!)
                    val reqFile = photoFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                    val body =
                        MultipartBody.Part.createFormData("profile_pic", photoFile!!.name, reqFile)
                    multiBody.add(body)
                }
                observeEditProfileData(hashMap, multiBody)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Logger.print("Error : $error")
            }
        }
    }

    private fun observeEditProfileData(
        hashMap: HashMap<String, RequestBody>,
        multiBody: List<MultipartBody.Part>?
    ) {
        viewModel.observeEditProfileData(this, hashMap, multiBody).observe(this, Observer {
            if (it != null) {
                Logger.print("New sign up ======$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val data = jsonObject.getJSONObject("data")
                        userProfileResponse = Gson().fromJson(
                            data.toString(),
                            object : TypeToken<UpdateProfileResponse>() {}.type
                        )

                        Picasso.with(this).load(userProfileResponse.profile_pic).fit().centerCrop()
                            .placeholder(R.drawable.userprofilenotification)
                            .error(R.drawable.userprofilenotification)
                            .into(iv_pic)

                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.PROFILE_PIC_PREF, userProfileResponse.profile_pic)
                    }
                } else if (code != 2) {
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                }
            }
        })
    }

    override fun onError(error: String?) {
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        public fun parseDate(date_select: String, edt_bod: TextInputEditText) {
            var format = SimpleDateFormat("yyyy-MM-dd")
            var date1: Date? = null
            try {
                date1 = format.parse(date_select)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val date = format.format(date1!!)
            format =
                if (date.endsWith("01") || date.endsWith("21") || date.endsWith("31") && !date.endsWith(
                        "11"
                    )
                ) SimpleDateFormat("d'st' MMM, yyyy")
                else if (date.endsWith("03") || date.endsWith("23") && !date.endsWith("13")) SimpleDateFormat(
                    "d'rd' MMM, yyyy"
                )
                else if (date.endsWith("02") || date.endsWith("22") && !date.endsWith("12")) SimpleDateFormat(
                    "d'nd' MMM, yyyy"
                )
                else SimpleDateFormat("d'th' MMM, yyyy")
            val yourDate = format.format(date1)
            edt_bod.setText(yourDate)
            Logger.print("yourDate!!!!!!!!!!!!!!!$yourDate")

            //  edt_date_of_birth.setText(yourDate)
        }


        @SuppressLint("SimpleDateFormat")
        public fun changeDateFormat(date_select: String): String? {
            var selectedDate = ""
            if (date_select != "") {
                var format =
                    if (date_select.contains("st")) SimpleDateFormat("d'st' MMM, yyyy")
                    else if (date_select.contains("rd")) SimpleDateFormat("d'rd' MMM, yyyy")
                    else if (date_select.contains("nd")) SimpleDateFormat("d'nd' MMM, yyyy")
                    else SimpleDateFormat("d'th' MMM, yyyy")

                Logger.print("changeDateFormat !!!!!!!$format")
                var date1: Date? = null
                try {
                    date1 = format.parse(date_select)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                format = SimpleDateFormat("yyyy-MM-dd")
                selectedDate = format.format(date1!!)

                Logger.print("Date changeDateFormat!!!!!!!!!!!!$selectedDate")
            }

            return selectedDate

        }
    }

    private fun showSpinnerDatePickerDialog() {
        val str = changeDateFormat(edt_bod.text.toString())

        if (str != "") {
            if (str!!.contains("-")) {
                val lstValues: List<String> = str.split("-")
                Logger.print("lstValues!!!!!!!!!!$lstValues")
                mYear = lstValues[0].toInt()
                mMonth = if (lstValues[1].toInt() != 0) {
                    lstValues[1].toInt() - 1
                } else {
                    lstValues[1].toInt()
                }
                mDay = lstValues[2].toInt()
            }


        } else {
            val c = Calendar.getInstance()
            mYear = c[Calendar.YEAR]
            mMonth = c[Calendar.MONTH]
            mDay = c[Calendar.DAY_OF_MONTH]
        }

        SupportedDatePickerDialog(this, R.style.DialogTheme, this, mYear, mMonth, mDay).show()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        mYear = year
        mMonth = month
        mDay = dayOfMonth
        Logger.print("mYear ==$mYear === mMonth $mMonth ===mDay$mDay")
        dateTime.set(mYear, month, dayOfMonth)
        val selectDateInMilliSeconds: Long = dateTime.timeInMillis
        val currentDate = Calendar.getInstance()
        val currentDateInMilliSeconds = currentDate.timeInMillis
        val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val strDt = simpleDate.format(dateTime.time)
        val diffDate = currentDateInMilliSeconds - selectDateInMilliSeconds
        val yourAge = Calendar.getInstance()
        yourAge.timeInMillis = diffDate
        val returnedYear = (yourAge[Calendar.YEAR] - 1970).toLong()

        when {
            selectDateInMilliSeconds > currentDateInMilliSeconds -> {
                Toast.makeText(this, R.string.validate_age_, Toast.LENGTH_LONG).show()
                return
            }
            returnedYear < allowedDOB -> {
                Toast.makeText(this, R.string.validate_age, Toast.LENGTH_LONG).show()
                return
            }
            else -> {
                parseDate(strDt, edt_bod)

                // httrydsfd
                //edtBirthdate.setText(strDt)
            }
        }
    }

}