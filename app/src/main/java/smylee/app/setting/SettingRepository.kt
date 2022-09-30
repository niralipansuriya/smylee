package smylee.app.setting

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.*

class SettingRepository : BaseRepository() {

    fun observeSignOut(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SIGNOUT, activity, APIConstants.POST_API,
            reqHashMap, true, false, null
        )
    }

    fun observeAllowNotifyOrNot(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEUSERPROFILE, activity, APIConstants.POST_API,
            reqHashMap, true, false, null
        )
    }
}