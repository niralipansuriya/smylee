package smylee.app.ChangePassword

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import smylee.app.utils.Logger
import smylee.app.utils.Utils
import smylee.app.utils.Utils.isEqualTexts
import smylee.app.utils.Utils.isPasswordValid
import kotlinx.android.synthetic.main.activity_change_password.*
import org.json.JSONObject
import smylee.app.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ChangePassword : BaseActivity() {


    private var changePasswordViewModel: ChangePasswordViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

//        supportActionBar!!.hide()

        changePasswordViewModel =
            ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)

        back.setOnClickListener {
            onBackPressed()
        }

        btn_submit.setOnClickListener {
            if (isVeified()) {
                var encrypted_old_psw = md5(edt_old_psw.text.toString())
                var encrypted_new_psw = md5(edt_new_psw.text.toString())

                if (!encrypted_old_psw!!.contentEquals("") && !encrypted_new_psw!!.contentEquals("")) {
                    callForChangePassword(encrypted_old_psw, encrypted_new_psw)

                }
            }
        }
    }

    fun md5(s: String): String? {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest = MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
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

    private fun callForChangePassword(encrypted_old_psw: String, encrypted_new_psw: String) {

        val hashMap = HashMap<String, String>()
        hashMap["old_password"] = encrypted_old_psw
        hashMap["new_password"] = encrypted_new_psw
//        hashMap["old_password"]=edt_old_psw.text.toString().trim()
//        hashMap["new_password"]=edt_new_psw.text.toString().trim()
        changePasswordViewModel?.observeChangePassword(
            this,
            hashMap
        )?.observe(
            this, Observer {
                if (it != null) {
                    Logger.print("change password response == >" + it.toString())
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        Utils.showToastMessage(this, jsonObject.getString("message"))

                        onBackPressed()
                    } else if (code == 0) {
                        Utils.showToastMessage(this, jsonObject.getString("message"))

                    }
                }
            }
        )

    }

    private fun isVeified(): Boolean {
        if (Utils.isTextEmpty(edt_old_psw)) {
            Utils.showToastMessage(this, getString(R.string.fill_current_password))
            return false
        } else if (!isPasswordValid(edt_old_psw.text.toString().trim())) {
            Utils.showToastMessage(
                this, getString(R.string.current_pass_6_digit_validation)
            )
            return false
        } else if (Utils.isTextEmpty(edt_new_psw)) {
            Utils.showToastMessage(this, getString(R.string.fill_newpassword))
            return false
        } else if (!isPasswordValid(edt_new_psw.text.toString().trim())) {
            Utils.showToastMessage(
                this, getString(R.string.new_6_digit_validation)
            )
            return false
        } else if (Utils.isTextEmpty(edt_confirm_new_psw)) {
            Utils.showToastMessage(this, getString(R.string.fill_cnfpassword))
            return false
        } else if (!isEqualTexts(edt_new_psw, edt_confirm_new_psw)) {
            Utils.showToastMessage(
                this, getString(R.string.fill_valid_confirmpassword)
            )
            return false
        }
        return true
    }
}