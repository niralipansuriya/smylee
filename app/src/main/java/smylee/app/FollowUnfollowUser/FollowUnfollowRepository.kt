package smylee.app.FollowUnfollowUser

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.*

class FollowUnfollowRepository : BaseRepository() {
    fun ObservFollowUnfollowList(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWERLISTUSER,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow, true,null)
    }

    fun ObserveFollowUnfollow(activity: BaseActivity, reqHashMap: HashMap<String, String>): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWUNFOLLOWUSER,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false, null)
    }
}