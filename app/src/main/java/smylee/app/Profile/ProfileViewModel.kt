package smylee.app.Profile

import android.app.Activity
import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import smylee.app.setting.SettingRepository
import java.util.HashMap

class ProfileViewModel : ViewModel() {

    fun blockunblockuser(
        activity: Activity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return ProfileRepository().obserblockunblockuser(activity, reqHashMap, isProgressShow)
    }

    fun signOut(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return ProfileRepository().observeSignOut(activity, reqHashMap)
    }


    fun observeGetProfile(
        activity: Activity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return ProfileRepository().getUserProfile(activity, reqHashMap, isProgressShow)
    }

    fun observeUpdateProfile(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): LiveData<String> {
        return ProfileRepository().UpdateUserProfile(activity, reqHashMap, isProgressShow)
    }


    fun observeEditProfileData(
        activity: BaseActivity,
        hashMap: HashMap<String, RequestBody>,
        multiBody: List<MultipartBody.Part>?
    ): LiveData<String> {
        return ProfileRepository().observeEditProfileData(activity, hashMap, multiBody)
    }

    fun FollowUnfollow(
        activity: Activity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return ProfileRepository().ObserveFollowUnfollow(activity, reqHashMap)
    }

    fun DeletePost(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return ProfileRepository().observeDeletePost(activity, reqHashMap)
    }


}