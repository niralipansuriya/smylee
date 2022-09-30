package smylee.app.home

import android.app.Activity
import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class HomeRepository : BaseRepository() {
    fun observeGetComments(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.GETCOMMENTS,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun observeDeletePost(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.DELETEPOST,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false, null)
    }

    fun observeDiscoverDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.DISCOVERVIEWALLCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun observeAddComments(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.ADDVIDEOCOMMENT,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun observerdiscovercategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FORYOULISTING,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun observeFlagAPI(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FLAGVIDEOORCOMMENT,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun observeFollowingAPI(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWINGVIDEOLIST,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun observeLikeUnlikeComments(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.LIKEUNLIKEcomments,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun ObserveFollowUnfollow(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWUNFOLLOWUSER,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false, null
        )
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
            false, null
        )
    }

    fun getUserProfile(
        activity: Activity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.GETUSERANDOTHERUSERPROFILE,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null
        )
    }

    fun videoDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.VIDEODETAILS,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null
        )
    }

    fun likeUnlikeVudeo(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.LIKEUNLIKEVIDEO,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null)
    }

    fun SendFcmToken(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SETUSERDEVICERELATION,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null
        )
    }

    fun observeHashTagDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.HASHTAGDETAIL,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false, null
        )
    }

}