package smylee.app.blockuser

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class BlockUserViewModel : ViewModel() {
    fun getBlockusers(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return BlockUserRepository().observeblockusers(activity, reqHashMap, isProgressShow)
    }

    fun blockunblockuser(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return BlockUserRepository().obserblockunblockuser(
            activity,
            reqHashMap,
            isProgressShow
        )
    }


}