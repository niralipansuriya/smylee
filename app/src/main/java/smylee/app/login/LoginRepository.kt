package smylee.app.login

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.*

class LoginRepository : BaseRepository() {

    fun observeLogin(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SIGNIN,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
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
            false,null)
    }

    fun observeUpdatecategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEUSERCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    fun observeVideoLanguage(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEVIDEOLANGUAGE,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    fun observeSignIn(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.SIGNINWITHPHONENUMBER,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }

    fun observeGuest(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.USERPROCEEDASGUEST,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }

    fun observeToken(
        activity: BaseActivity
    ): MutableLiveData<String> {
        return callAPIRefreshToken(
            APIConstants.REFRESH_TOKEN_API,
            activity, APIConstants.POST_API, true,
            false,null
        )
    }
}