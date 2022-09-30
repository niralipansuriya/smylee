package smylee.app.FollowUnfollowUser

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class FollowUnfollowViewModel : ViewModel() {

    fun FollowerList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return FollowUnfollowRepository().ObservFollowUnfollowList(
            activity,
            reqHashMap,
            isProgressShow
        )
    }


    fun FollowUnfollow(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return FollowUnfollowRepository().ObserveFollowUnfollow(activity, reqHashMap)
    }

}