package smylee.app.signup

import android.annotation.SuppressLint
import smylee.app.ui.base.BaseActivity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import smylee.app.model.SignupResponse
import smylee.app.otpverification.VerificstionActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import smylee.app.utils.Utils.isEmailValid
import smylee.app.utils.Utils.isEqualTexts
import smylee.app.utils.Utils.isPasswordValid
import smylee.app.utils.Utils.isTextEmpty
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.edt_email
import org.json.JSONObject
import smylee.app.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class RegisterActivity : BaseActivity() {
    private var datePickerFragment: DatePickerFragment? = null

    private lateinit var signUpViewModel: SignUpViewModel
    private var myCalendar: Calendar? = null
    private var genderTypeStr: String = "" //M: Male F:Female B:Both
    private lateinit var signUpResponse: SignupResponse
    private var deviceId: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        myCalendar = Calendar.getInstance()

        signUpViewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)
        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        if (!deviceId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.DEVICE_ID, deviceId)
        }
        Logger.print("DEVICE_ID=============", deviceId)

        tv_login.setOnClickListener {
            finish()
        }

        /*val colorStateList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                R.color.white //disabled
                , R.color.purple //enabled
            )
        )*/

        rb_male.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "1"
            }
        }
        rb_female.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "2"
            }
        }
        rb_male.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "1"
            }
        }
        rb_female.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                genderTypeStr = "2"
            }
        }

        edt_dob.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                datePickerFragment = DatePickerFragment(edt_dob)
                datePickerFragment!!.show(supportFragmentManager, "Select Your Birthday")
                return@OnTouchListener true
            }
            false
        })

        btn_signup.setOnClickListener {
            if (isTextEmpty(edt_firstname)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_first_name)
                )
            } else if (isTextEmpty(edt_last_name)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_last_name)
                )
            } else if (genderTypeStr.contentEquals("")) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.select_gender)
                )
            } else if (isTextEmpty(edt_dob)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_birth_date)
                )
            } else if (isTextEmpty(edt_email)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_email)
                )
            } else if (!isEmailValid(edt_email.text.toString().trim())) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_valid_email)
                )
            } else if (isTextEmpty(edt_psw)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_password)
                )
            } else if (!isPasswordValid(edt_psw.text.toString().trim())) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.pass_6_digit_validation)
                )
            } else if (isTextEmpty(edt_confirm_psw)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_cnfpassword)
                )
            } else if (!isEqualTexts(edt_psw, edt_confirm_psw)) {
                Utils.showAlert(
                    context = this@RegisterActivity,
                    title = "",
                    message = getString(R.string.fill_valid_confirmpassword)
                )
            } else {
                val encryptedPsw = md5(edt_psw.text.toString())
                if (!encryptedPsw!!.contentEquals("")) {
                    callForRegister(encryptedPsw)
                }
            }
        }
    }

    class DatePickerFragment(private var edtBirthDate: AppCompatEditText) : DialogFragment(),
        OnDateSetListener {
        private var mYear: Int = 0
        private var mMonth: Int = 0
        private var mDay: Int = 0
        private val dateTime = Calendar.getInstance()
        private val allowedDOB: Int = 13

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Using current date as start Date
            val c = Calendar.getInstance()
            mYear = c[Calendar.YEAR]
            mMonth = c[Calendar.MONTH]
            mDay = c[Calendar.DAY_OF_MONTH]
            // Get DatePicker Dialog
            return DatePickerDialog(context!!, R.style.DialogTheme, this, mYear, mMonth, mDay)
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
            // edtBirthdate.setText(strDt)
            val diffDate = currentDateInMilliSeconds - selectDateInMilliSeconds
            val yourAge = Calendar.getInstance()
            yourAge.timeInMillis = diffDate
            val returnedYear = (yourAge[Calendar.YEAR] - 1970).toLong()

            when {
                selectDateInMilliSeconds > currentDateInMilliSeconds -> {
                    Toast.makeText(
                        activity,
                        "Your birthday date must come before taday's date",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                returnedYear < allowedDOB -> {
                    Toast.makeText(
                        activity,
                        "Sorry!!! You are not allowed to use this app",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                else -> {
                    edtBirthDate.setText(strDt)
                }
            }
        }
    }


    /*val date1 = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar!!.set(Calendar.YEAR, year)
            myCalendar!!.set(Calendar.MONTH, monthOfYear)
            myCalendar!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel1(edt_dob)
        }*/

    /*private fun updateLabel1(edtLeaveFromDate: EditText) {
        val myFormat1 = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat1, Locale.US)
        val sdf1 = SimpleDateFormat(myFormat1, Locale.US)
        edtLeaveFromDate.setText(sdf.format(myCalendar!!.time) + "")
    }*/

    private fun md5(s: String): String? {
        val md5 = "MD5"
        try {
            val digest = MessageDigest.getInstance(md5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun callForRegister(encrypted_psw: String) {
        val hashMap = HashMap<String, String>()
        hashMap["first_name"] = edt_firstname.text.toString().trim()
        hashMap["last_name"] = edt_last_name.text.toString().trim()
        hashMap["password"] = encrypted_psw
        hashMap["email_id"] = edt_email.text.toString().trim()
        hashMap["date_of_birth"] = edt_dob.text.toString().trim()
        hashMap["gender"] = genderTypeStr

        signUpViewModel.observerSignupcall(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Signup response : $it")
                try {
                    val jsonObject = JSONObject(it.toString())
                    if (jsonObject["code"] == 1) {
                        SharedPreferenceUtils.getInstance(this@RegisterActivity)
                            .setValue(
                                Constants.AUTH_TOKEN_PREF,
                                jsonObject["auth_token"].toString()
                            )

                        if (jsonObject.has("data")) {
                            val data = jsonObject.getJSONObject("data")
                            signUpResponse =
                                Gson().fromJson(data.getString("user").toString(), object :
                                    TypeToken<SignupResponse>() {}.type)
                            SharedPreferenceUtils.getInstance(this@RegisterActivity)

                            if (signUpResponse.getEmailID() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(Constants.EMAILPREF, signUpResponse.getEmailID()!!)
                            }

                            if (signUpResponse.getfirst_name() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.FIRST_NAME_PREF,
                                        signUpResponse.getfirst_name()!!
                                    )
                            }

                            if (signUpResponse.getlast_name() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.LAST_NAME_PREF,
                                        signUpResponse.getlast_name()!!
                                    )
                            }

                            if (signUpResponse.getcategory_list() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.CATEGORY_PREF,
                                        signUpResponse.getcategory_list()!!
                                    )
                            }

                            if (signUpResponse.getlanguage() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.LANGUAGE_PREF,
                                        signUpResponse.getlanguage()!!
                                    )
                            }

                            if (signUpResponse.getuser_name() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.USERNAME_PREF,
                                        signUpResponse.getuser_name()!!
                                    )
                            }
                            if (signUpResponse.getcountry() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(Constants.COUNTRY_PREF, signUpResponse.getcountry()!!)
                            }

                            if (signUpResponse.getuser_register_status() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.USER_REGISTER_STATUS_PREF,
                                        signUpResponse.getuser_register_status()!!
                                    )
                            }

                            if (signUpResponse.getis_verified() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.IS_VERIFIED_PREF,
                                        signUpResponse.getis_verified()!!
                                    )
                            }
                            if (signUpResponse.getdate_of_birth() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.DOB_PREF,
                                        signUpResponse.getdate_of_birth()!!
                                    )
                            }

                            if (signUpResponse.getno_of_followings() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.FOLLOWING_COUNT_PREF,
                                        signUpResponse.getno_of_followings()!!
                                    )
                            }

                            if (signUpResponse.getno_of_followers() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.FOLLOWER_COUNT_PREF,
                                        signUpResponse.getno_of_followers()!!
                                    )
                            }

                            if (signUpResponse.getorganisation_name() != null) {
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(
                                        Constants.ORGANISATION_NAME_PREF,
                                        signUpResponse.getorganisation_name()!!
                                    )
                            }

                            val intent = Intent(this, VerificstionActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else if (jsonObject["code"] == 0) {
                        Utils.showAlert(this@RegisterActivity, "", jsonObject["message"].toString())
                    } else {
                        Utils.showAlert(this@RegisterActivity, "", jsonObject["message"].toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }
}