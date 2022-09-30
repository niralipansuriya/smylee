package smylee.app.setting

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SettingViewModel : ViewModel() {

    fun signOut(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SettingRepository().observeSignOut(activity, reqHashMap)
    }

    fun allowNotifyOrNot(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SettingRepository().observeAllowNotifyOrNot(activity, reqHashMap)
    }
}