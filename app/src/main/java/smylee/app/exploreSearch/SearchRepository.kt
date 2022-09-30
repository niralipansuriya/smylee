package smylee.app.exploreSearch

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class SearchRepository : BaseRepository() {
    fun observeSearch(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SEARCHVIDEOLIST,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun observVideos(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SEARCHVIEWMOREVIDEOS,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun ObserveFollowUnfollow(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWUNFOLLOWUSER,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    fun observePeople(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SEARCHVIEWMOREPEOPLE,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

}