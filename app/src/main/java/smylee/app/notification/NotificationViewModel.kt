package smylee.app.notification

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    fun observeNotificationList(
        activity: BaseActivity,
        hashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return NotificationRepository().observeNotificationList(activity, hashMap, isProgressShow)
    }

}