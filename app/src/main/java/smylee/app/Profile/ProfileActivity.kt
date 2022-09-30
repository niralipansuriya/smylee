package smylee.app.Profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ibotta.android.support.pickerdialogs.SupportedDatePickerDialog
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.model.UpdateProfileResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ProfileActivity : BaseActivity(), CameraUtils.OnCameraResult,
    SupportedDatePickerDialog.OnDateSetListener {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var userprofileresponse: UpdateProfileResponse
    private var photoFile: File? = null

    //    private var mCurrentPhotoPath: String? = null
    /*private var selectedBitmap: Bitmap? = null
    private var apiService: APICalls? = null*/
    var hashMap = HashMap<String, RequestBody>()
    private var cameraUtils: CameraUtils? = null
 //   private var datePickerFragment: DatePickerFragment? = null

    private var myCalendar: Calendar? = null
    private var myCalendar2: GregorianCalendar? = null
    private var genderTypeStr: String = "" //M: Male F:Female B:Both

    private var FIRST_NAME: String = ""
    private var FACEBOOK_URL: String = ""
    private var INSTAGRAM_URL: String = ""
    private var YOUTUBE_URl: String = ""
    private var LAST_NAME: String = ""
    private var EMAIL_ID: String = ""
    private var BIRTH_DATE: String = ""
    private var GENDER: String = ""

    private var DEVICE_MANUFACTURER: String = ""


    /*added for lib*/
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private val dateTime = Calendar.getInstance()
    private val allowedDOB: Int = 13


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        Utils.hideKeyboard(this)

        myCalendar = Calendar.getInstance()
        myCalendar2 = GregorianCalendar()
        cameraUtils = CameraUtils(this, this)

        DEVICE_MANUFACTURER = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.MANUFACTURER, "")!!

        //  apiService = ApiClient.getClient().create(APICalls::class.java)
        back.setOnClickListener {
            onBackPressed()
        }

        FIRST_NAME = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.FIRST_NAME_PREF, "").toString()

        FACEBOOK_URL = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.FACEBOOK_URL, "").toString()

        YOUTUBE_URl = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.YOUTUBE_URL, "").toString()

        INSTAGRAM_URL = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.INSTAGRAM_URL, "").toString()

        EMAIL_ID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.EMAILPREF, "").toString()

        LAST_NAME = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LAST_NAME_PREF, "").toString()

        BIRTH_DATE = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DOB_PREF, "").toString()

        GENDER = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getIntValue(Constants.GENDER_PREF, 0).toString()

        if (!FACEBOOK_URL.contentEquals("null")) {
            edt_facebook.setText(FACEBOOK_URL)
        }
        if (!YOUTUBE_URl.contentEquals("null")) {
            edt_youtube.setText(YOUTUBE_URl)
        }
        if (!INSTAGRAM_URL.contentEquals("null")) {
            edt_instagram.setText(INSTAGRAM_URL)
        }
        edt_first_name.setText(FIRST_NAME)
        edt_lastname.setText(LAST_NAME)

        if (!EMAIL_ID.contentEquals("")) {
            edt_email.setText(EMAIL_ID)
        }
        // edt_bod.setText(BIRTH_DATE)
        parseDate(BIRTH_DATE, edt_bod)
        //changeDateFormat(edt_bod.text.toString())
        if (GENDER.contentEquals("1")) {
            rb_male_profile.isChecked = true
            genderTypeStr = "1"
        } else {
            rb_male_profile.isChecked = false
        }

        if (GENDER.contentEquals("2")) {
            rb_female_profile.isChecked = true
            genderTypeStr = "2"
        } else {
            rb_female_profile.isChecked = false
        }

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
        })*/
        val intentFilter = IntentFilter("errorMessageProfileUpload")
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadingMessage, intentFilter)

        val profilePhoto: String =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.PROFILE_PIC_PREF, "")!!

        if (!profilePhoto.contentEquals("") && !profilePhoto.contentEquals("null")) {
            Glide.with(this)
                .load(profilePhoto)
                .error(R.drawable.userprofilenotification)
                .into(iv_pic)
        }

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
            }
//            else if (Utils.isTextEmpty(edt_lastname)) {
//                Utils.showAlert(context = this, title = "", message = getString(R.string.fill_last_name))
//            }
            else if (edt_email.text.toString().trim() != "") {
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
                } else if (edt_facebook.text!!.toString().trim() != "") {
                    if (!edt_facebook.text.toString().trim()
                            .contains("https") || !edt_facebook.text!!.toString().trim()
                            .contains("facebook")
                    ) {
                        Utils.showAlert(
                            context = this,
                            title = "",
                            message = getString(R.string.facebook_link)
                        )
                    } else {
                        if (edt_instagram.text!!.toString().trim() != "") {
                            if (!edt_instagram.text!!.toString().trim()
                                    .contains("https") || !edt_instagram.text!!.toString().trim()
                                    .contains("instagram")
                            ) {
                                Utils.showAlert(
                                    context = this,
                                    title = "",
                                    message = getString(R.string.instagram_link)
                                )
                            } else {
                                if (edt_youtube.text!!.toString().trim() != "") {
                                    if (!edt_youtube.text!!.toString().trim().contains("https") ||
                                        !edt_youtube.text!!.toString().trim().contains("youtube")
                                    ) {
                                        Utils.showAlert(
                                            context = this,
                                            title = "",
                                            message = getString(R.string.youtube_link)
                                        )
                                    } else {
                                        UpdateUserProfile()
                                    }
                                } else {
                                    UpdateUserProfile()
                                }
                            }
                        } else {
                            if (edt_youtube.text!!.toString().trim() != "") {
                                if (!edt_youtube.text!!.toString().trim().contains("https") ||
                                    !edt_youtube.text!!.toString().trim().contains("youtube")
                                ) {
                                    Utils.showAlert(
                                        context = this,
                                        title = "",
                                        message = getString(R.string.youtube_link)
                                    )
                                } else {
                                    UpdateUserProfile()
                                }
                            } else {
                                UpdateUserProfile()
                            }
                        }
                    }
                } else if (edt_instagram.text!!.toString().trim() != "") {
                    if (!edt_instagram.text!!.toString().trim().contains("https") ||
                        !edt_instagram.text!!.toString().trim().contains("instagram")
                    ) {
                        Utils.showAlert(
                            context = this,
                            title = "",
                            message = getString(R.string.instagram_link)
                        )
                    } else {
                        if (edt_youtube.text!!.toString().trim() != "") {
                            if (!edt_youtube.text!!.toString().trim().contains("https") ||
                                !edt_youtube.text!!.toString().trim().contains("youtube")
                            ) {
                                Utils.showAlert(
                                    context = this,
                                    title = "",
                                    message = getString(R.string.youtube_link)
                                )
                            } else {
                                UpdateUserProfile()
                            }
                        } else {
                            UpdateUserProfile()
                        }
                    }
                } else if (edt_youtube.text!!.toString().trim() != "") {
                    if (!edt_youtube.text!!.toString().trim().contains(
                            "https"
                        ) || !edt_youtube.text!!.toString().trim().contains("youtube")
                    ) {
                        Utils.showAlert(
                            context = this,
                            title = "",
                            message = getString(R.string.youtube_link)
                        )
                    } else {
                        UpdateUserProfile()
                    }
                } else {
                    UpdateUserProfile()
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
            } else if (edt_facebook.text!!.toString().trim() != "") {
                if (!edt_facebook.text.toString().trim().contains("https") ||
                    !edt_facebook.text!!.toString().trim().contains("facebook")
                ) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.facebook_link)
                    )
                } else {
                    if (edt_instagram.text!!.toString().trim() != "") {
                        if (!edt_instagram.text!!.toString().trim().contains("https") ||
                            !edt_instagram.text!!.toString().trim().contains("instagram")
                        ) {
                            Utils.showAlert(
                                context = this,
                                title = "",
                                message = getString(R.string.instagram_link)
                            )
                        } else {
                            if (edt_youtube.text!!.toString().trim() != "") {
                                if (!edt_youtube.text!!.toString().trim().contains("https") ||
                                    !edt_youtube.text!!.toString().trim().contains("youtube")
                                ) {
                                    Utils.showAlert(
                                        context = this,
                                        title = "",
                                        message = getString(R.string.youtube_link)
                                    )
                                }
                            } else {
                                UpdateUserProfile()
                            }
                        }
                    } else {
                        if (edt_youtube.text!!.toString().trim() != "") {
                            if (!edt_youtube.text!!.toString().trim().contains("https") ||
                                !edt_youtube.text!!.toString().trim().contains("youtube")
                            ) {
                                Utils.showAlert(
                                    context = this,
                                    title = "",
                                    message = getString(R.string.youtube_link)
                                )
                            }
                        } else {
                            UpdateUserProfile()
                        }
                    }
                }
            } else if (edt_instagram.text!!.toString().trim() != "") {
                if (!edt_instagram.text!!.toString().trim().contains("https") ||
                    !edt_instagram.text!!.toString().trim().contains("instagram")
                ) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.instagram_link)
                    )
                } else {
                    if (edt_youtube.text!!.toString().trim() != "") {
                        Utils.showAlert(
                            context = this,
                            title = "",
                            message = getString(R.string.youtube_link)
                        )
                    } else {
                        UpdateUserProfile()
                    }
                }
            } else if (edt_youtube.text!!.toString().trim() != "") {
                if (!edt_youtube.text!!.toString().trim().contains(
                        "https"
                    ) || !edt_youtube.text!!.toString().trim().contains("youtube")
                ) {
                    Utils.showAlert(
                        context = this,
                        title = "",
                        message = getString(R.string.youtube_link)
                    )
                } else {
                    UpdateUserProfile()
                }
            } else {
                UpdateUserProfile()
            }
        }

        iv_edit.setOnClickListener {
            cameraUtils?.openCameraGallery()
        }
    }




    private val uploadingMessage = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("apiErrorMessageForUpload")!!
            showDialogForPostVideo(message)
        }
    }

    private fun showDialogForPostVideo(message: String) {
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
        val tvEditProfile = dialogView.findViewById<View>(R.id.tv_edit_profile) as TextView
        tvEditProfile.text = message
        show.setOnDismissListener {
            setResult(Activity.RESULT_OK)
        }

        tvNo.setOnClickListener {
            show.dismiss()
        }

        tvYes.setOnClickListener {
            show.dismiss()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadingMessage)

        super.onDestroy()
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
    private fun observeEditProfileData(
        hashMap: HashMap<String, RequestBody>,
        multiBody: List<MultipartBody.Part>?
    ) {
        viewModel.observeEditProfileData(this, hashMap, multiBody).observe(this, Observer {
            if (it != null) {

                Logger.print("UPDATE USER PROFILE PIC RESPONSE======$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val data = jsonObject.getJSONObject("data")
                        userprofileresponse = Gson().fromJson(
                            data.toString(),
                            object : TypeToken<UpdateProfileResponse>() {}.type
                        )
                        SharedPreferenceUtils.getInstance(this@ProfileActivity)
                            .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                        SharedPreferenceUtils.getInstance(applicationContext)
                            .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                        Picasso.with(this).load(userprofileresponse.profile_pic).fit().centerCrop()
                            .placeholder(R.drawable.userprofilenotification)
                            .error(R.drawable.userprofilenotification)
                            .into(iv_pic)

                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.PROFILE_PIC_PREF, userprofileresponse.profile_pic)

                        SharedPreferenceUtils.getInstance(this).setValue(
                            Constants.CROPED_PROFILE_PIC,
                            userprofileresponse.profile_pic_compres
                        )
                    }
                } else if (code != 2) {
                    // showDialogForPostVideo(jsonObject.getString("message"))
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                }
            }
        })
    }

    private fun UpdateUserProfile() {
        Logger.print("edt_facebook.text!! length *************" + edt_facebook.text!!.length)
        val hashMap = HashMap<String, String>()
        if (edt_first_name.text.toString().isNotEmpty()) {
            hashMap["first_name"] = edt_first_name.text.toString().trim()
        }
        //if (edt_lastname.text.toString().isNotEmpty()) {
        hashMap["last_name"] = edt_lastname.text.toString().trim()
        //  }
        // if (edt_email.text.toString().isNotEmpty()) {
        hashMap["email_id"] = edt_email.text.toString().trim()
        //}
        if (edt_bod.text.toString().isNotEmpty()) {
            var finalDate = changeDateFormat(edt_bod.text.toString())
            //  hashMap["date_of_birth"] = edt_bod.text.toString().trim()
            hashMap["date_of_birth"] = finalDate.toString().trim()
        }
        hashMap["facebook_url"] = edt_facebook.text.toString().trim()
        hashMap["instagram_url"] = edt_instagram.text.toString().trim()
        hashMap["youtube_url"] = edt_youtube.text.toString().trim()

        viewModel.observeUpdateProfile(this, hashMap, true).observe(this, Observer {
            if (it != null) {
                Logger.print("UpdateUserProfile response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        SharedPreferenceUtils.getInstance(applicationContext)
                            .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                        if (jsonObject.has("data")) {
                            val data = jsonObject.getJSONObject("data")
                            userprofileresponse = Gson().fromJson(
                                data.toString(),
                                object : TypeToken<UpdateProfileResponse>() {}.type
                            )
                            SharedPreferenceUtils.getInstance(this)

                            if (userprofileresponse.email_id != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(Constants.EMAILPREF, userprofileresponse.email_id!!)
                            }
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.FIRST_NAME_PREF, userprofileresponse.first_name)
                            if (userprofileresponse.facebook_url != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FACEBOOK_URL,
                                    userprofileresponse.facebook_url!!
                                )
                            }
                            if (userprofileresponse.instagram_url != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.INSTAGRAM_URL,
                                    userprofileresponse.instagram_url!!
                                )
                            }
                            if (userprofileresponse.youtube_url != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.YOUTUBE_URL,
                                    userprofileresponse.youtube_url!!
                                )
                            }
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.LAST_NAME_PREF,
                                userprofileresponse.last_name
                            )
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.GENDER_PREF, userprofileresponse.gender)
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.DOB_PREF, userprofileresponse.date_of_birth)
                            if (userprofileresponse.blocked_user_id != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.BLOCK_USER_PREF,
                                    userprofileresponse.blocked_user_id!!
                                )
                            }
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.IS_BLOCKED_PREF,
                                userprofileresponse.is_blocked
                            )
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.IS_VERIFIED_PREF,
                                userprofileresponse.is_verified
                            )
                            if (userprofileresponse.no_of_followers != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWER_COUNT_PREF,
                                    userprofileresponse.no_of_followers!!
                                )
                            }
                            if (userprofileresponse.no_of_followings != null) {
                                SharedPreferenceUtils.getInstance(this).setValue(
                                    Constants.FOLLOWING_COUNT_PREF,
                                    userprofileresponse.no_of_followings!!
                                )
                            }
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.PROFILE_PIC_PREF,
                                userprofileresponse.profile_pic
                            )
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.LANGUAGE_PREF, userprofileresponse.language)
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.CATEGORY_PREF,
                                userprofileresponse.category_list
                            )
                            SharedPreferenceUtils.getInstance(this).setValue(
                                Constants.REG_ID_PREF,
                                userprofileresponse.user_register_status
                            )
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.USER_ID_PREF, userprofileresponse.user_id)
                            SharedPreferenceUtils.getInstance(applicationContext)
                                .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                            onBackPressed()
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

/*
    class DatePickerFragment(private var edtBirthdate: TextInputEditText) : DialogFragment(),
        DatePickerDialog.OnDateSetListener {
        private var mYear: Int = 0
        private var mMonth: Int = 0
        private var mDay: Int = 0
        private val dateTime = Calendar.getInstance()
        private val allowedDOB: Int = 13
        private var datePicker: DatePicker? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val str = changeDateFormat(edtBirthdate.text.toString())

            if (str != "") {
                if (str!!.contains("-")) {
                    val lstValues: List<String> = str.split("-")
                    Logger.print("lstValues!!!!!!!!!!$lstValues")
                    //Logger.print("0th index element!!!!${lstValues[0]}")
                    //Logger.print("1th index element!!!!${lstValues[1]}")
                    //  Logger.print("2th index element!!!!${lstValues[2]}")
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

                */
/*val view: View =
                    datePickerDialog.getLayoutInflater().inflate(R.layout.title_view, null)

                datePickerDialog.setCustomTitle(view)*//*

                // datePickerDialog.setTitle(resources.getString(R.string.selectdate))
                */
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[1].performClick();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    datePickerDialog.datePicker.touchables[0].performClick();
                }*//*

                return datePickerDialog
                //  return DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
            } else {
                val c = Calendar.getInstance()
                mYear = c[Calendar.YEAR]
                mMonth = c[Calendar.MONTH]
                mDay = c[Calendar.DAY_OF_MONTH]

                Logger.print("previous date !!!!!!!!${edtBirthdate.text.toString()}")
                Logger.print("mYearonCreateDialog ==$mYear === mMonthonCreateDialog $mMonth ===mDayonCreateDialog$mDay")
                val datePickerDialog =

                    DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
                // datePickerDialog.setTitle(resources.getString(R.string.selectdate))

                */
/*  val view: View =
                      datePickerDialog.getLayoutInflater().inflate(R.layout.title_view, null)
                  datePickerDialog.setCustomTitle(view)*//*

                */
/* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                     datePickerDialog.datePicker.touchables[1].performClick();
                 } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     datePickerDialog.datePicker.touchables[0].performClick();
                 }*//*

                return datePickerDialog
            }
        }

        override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            mYear = year
            mMonth = monthOfYear
            mDay = dayOfMonth
            Logger.print("mYear ==$mYear === mMonth $mMonth ===mDay$mDay")
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
                    parseDate(strDt, edtBirthdate)

                    // httrydsfd
                    //edtBirthdate.setText(strDt)
                }
            }
        }
    }
*/


    override fun onSuccess(images: MutableList<ChosenImage>?) {
        if (images != null && images.size > 0) {
            photoFile = File(images[0].originalPath)
            //binding.ivProfilePic.setImageURI(Uri.fromFile(selectedImageFile));
            CropImage.activity(Uri.fromFile(photoFile))
                .setAllowFlipping(false)
                .setFlipHorizontally(false)
                .setFlipVertically(false)
                .setFixAspectRatio(true)
                .setRequestedSize(600, 600)
                .start(this)

        }
    }

    override fun onError(error: String?) {
        Logger.print("error in pront!!!!!!!!$error")
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
                    try {
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true

                        BitmapFactory.decodeFile(photoFile!!.absolutePath, options)
                        val imageHeight: Int = options.outHeight
                        val imageWidth: Int = options.outWidth
                        Logger.print("imageHeight ==== ${imageHeight} imageWidth =======${imageWidth}")
                    } catch (e: Exception) {

                    }

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


    companion object {
        /* private var currentDate = Calendar.getInstance()
         private const val CURRENT_DATE_KEY = "currentDate"*/
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