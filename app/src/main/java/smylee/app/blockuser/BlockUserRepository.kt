package smylee.app.blockuser

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class BlockUserRepository : BaseRepository() {

    fun observeblockusers(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.GETBLOCKUSERS,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow, true,null)
    }

    fun obserblockunblockuser(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.BLOCKUNBLOCKUSER,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow, true, null)
    }
}