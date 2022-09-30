package smylee.app.otpverification

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class OtpViewModel : ViewModel() {

    fun observeVerifyOTP(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return OtpRepository().observeVerifyOTP(activity, reqHashMap)
    }

    fun observeVerifyChangeMobile(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return OtpRepository().observeVerifyChangeMobile(activity, reqHashMap)
    }

    fun observeResendOTP(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return OtpRepository().observeResendCode(activity, reqHashMap)
    }

}