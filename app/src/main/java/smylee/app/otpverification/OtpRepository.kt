package smylee.app.otpverification

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.*

class OtpRepository : BaseRepository() {

    fun observeVerifyOTP(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.VERIFYCODE,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }

    fun observeVerifyChangeMobile(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.VERIFYCHANGEMOBILE,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }

    fun observeResendCode(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.RESENDCODE,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }
}