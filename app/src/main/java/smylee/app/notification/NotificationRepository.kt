package smylee.app.notification

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class NotificationRepository : BaseRepository() {
    fun observeNotificationList(
        activity: BaseActivity,
        hashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.NOTIFICATIONLISTING, activity, APIConstants.POST_API,
            hashMap, isProgressShow,false,null)
    }

}