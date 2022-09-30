package smylee.app.exploreSearch

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    fun getSearchResults(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return SearchRepository().observeSearch(activity, reqHashMap, isProgressShow)
    }

    fun getVideoResult(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return SearchRepository().observVideos(activity, reqHashMap, isProgressShow)
    }

    fun getPeopleResult(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return SearchRepository().observePeople(activity, reqHashMap, isProgressShow)
    }

    fun FollowUnfollow(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SearchRepository().ObserveFollowUnfollow(activity, reqHashMap)
    }

}