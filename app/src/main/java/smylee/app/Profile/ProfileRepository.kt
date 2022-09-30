package smylee.app.Profile

import android.app.Activity
import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.HashMap

class ProfileRepository : BaseRepository() {

    fun obserblockunblockuser(
        activity: Activity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.BLOCKUNBLOCKUSER,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow, true, null
        )
    }

    fun observeSignOut(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SIGNOUT, activity, APIConstants.POST_API,
            reqHashMap, true, false, null
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

    fun UpdateUserProfile(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEUSERPROFILE,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun ObserveFollowUnfollow(
        activity: Activity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FOLLOWUNFOLLOWUSER,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    fun observeDeletePost(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.DELETEPOST,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    /*fun observeDeletePost(activity: smylee.app.ui.base.BaseActivity,hashMap: HashMap<String, RequestBody>
                               ,multiBody: List<MultipartBody.Part>?):MutableLiveData<String>{
        return callMultiPartAPI(
            APIConstants.DELETEPOST, activity, hashMap,
            APIConstants.DELETEPOST, true, false,
            false, APIConstants.POST_API, multiBody
        )
    }*/
    fun observeEditProfileData(
        activity: BaseActivity, hashMap: HashMap<String, RequestBody>
        , multiBody: List<MultipartBody.Part>?
    ): MutableLiveData<String> {
        return callMultiPartAPI(
            APIConstants.UPDATEUSERPROFILE, activity, hashMap,
            APIConstants.UPDATEUSERPROFILE, true, false,
            APIConstants.POST_API, multiBody
        )
    }
}