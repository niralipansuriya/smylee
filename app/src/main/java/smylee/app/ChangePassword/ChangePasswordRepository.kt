package smylee.app.ChangePassword

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class ChangePasswordRepository : BaseRepository() {

    fun observeChangePasswordData(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.CHANGEPASSWORD,
            activity,
            APIConstants.POST_API,
            hashMap,
            true,
            false,
            null
        )
    }

    fun ObserveChangeMobile(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.CHANGEMOBILE,
            activity,
            APIConstants.POST_API,
            hashMap,
            true,
            false,
            null
        )
    }

    fun ObserveVerifyMobile(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.VERIFYCHANGEMOBILE,
            activity,
            APIConstants.POST_API,
            hashMap,
            true,
            false,
            null
        )
    }

}