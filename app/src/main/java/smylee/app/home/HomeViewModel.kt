package smylee.app.home

import android.app.Activity
import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import smylee.app.Profile.ProfileRepository
import smylee.app.discovercategory.DiscoverRepository
import smylee.app.exploreSearch.SearchRepository
import smylee.app.trending.TrendingRepository

class HomeViewModel : ViewModel() {
    fun getComments(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): LiveData<String> {
        return HomeRepository().observeGetComments(activity, reqHashMap, isProgressShow)
    }

    fun DeletePost(
        activity: BaseActivity,
        reqHashMap: java.util.HashMap<String, String>
    ): LiveData<String> {
        return HomeRepository().observeDeletePost(activity, reqHashMap)
    }

    fun getDiscoverDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().observeDiscoverDetails(activity, reqHashMap, isProgressShow)
    }

    fun getVideoResult(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().observVideos(activity, reqHashMap, isProgressShow)
    }

    fun observeGetProfile(
        activity: Activity,
        reqHashMap: java.util.HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().getUserProfile(activity, reqHashMap, isProgressShow)
    }

    fun getVideoDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().videoDetails(activity, reqHashMap, isProgressShow)
    }

    fun FollowUnfollow(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return HomeRepository().ObserveFollowUnfollow(activity, reqHashMap)
    }

    fun AddComments(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().observeAddComments(activity, reqHashMap, isProgressShow)
    }

    fun LikeUnlikecomment(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): LiveData<String> {
        return HomeRepository().observeLikeUnlikeComments(activity, reqHashMap, isProgressShow)
    }

    fun likeUnlikeVideo(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): LiveData<String> {
        return HomeRepository().likeUnlikeVudeo(activity, reqHashMap, isProgressShow)
    }

    fun getdiscovercategory(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): LiveData<String> {
        return HomeRepository().observerdiscovercategory(activity, reqHashMap, isProgressShow)
    }

    fun FlageVideo(activity: BaseActivity, reqHashMap: HashMap<String, String>, isProgressShow: Boolean): LiveData<String> {
        return HomeRepository().observeFlagAPI(activity, reqHashMap, isProgressShow)
    }

    fun getFollowingAPI(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().observeFollowingAPI(activity, reqHashMap, isProgressShow)
    }

    fun SendFcmToken(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().SendFcmToken(activity, reqHashMap, isProgressShow)
    }

    fun observeHashTagDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return HomeRepository().observeHashTagDetail(activity, reqHashMap, isProgressShow)
    }
}