package smylee.app.ChangePassword

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ChangePasswordViewModel : ViewModel() {

    fun observeChangePassword(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): LiveData<String> {
        return ChangePasswordRepository().observeChangePasswordData(activity, hashMap)
    }

    fun observeChangeMobile(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): LiveData<String> {
        return ChangePasswordRepository().ObserveChangeMobile(activity, hashMap)
    }

    fun observeVerifyChangeMobile(
        activity: BaseActivity,
        hashMap: HashMap<String, String>
    ): LiveData<String> {
        return ChangePasswordRepository().ObserveVerifyMobile(activity, hashMap)
    }

}