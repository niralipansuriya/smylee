package smylee.app.login

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import smylee.app.home.HomeRepository

class LoginVIewModel : ViewModel() {

    fun getUpdatedUserLanguage(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return LoginRepository().observeVideoLanguage(activity, reqHashMap)
    }

    fun SendFcmToken(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return LoginRepository().SendFcmToken(activity, reqHashMap, isProgressShow)
    }

    fun getUpdatedUserCategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return LoginRepository().observeUpdatecategory(activity, reqHashMap)
    }

    fun observeLoginCall(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return LoginRepository().observeLogin(activity, reqHashMap)
    }

    fun observeSignINAPI(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return LoginRepository().observeSignIn(activity, reqHashMap)
    }

    fun userProceedAsGuest(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return LoginRepository().observeGuest(activity, reqHashMap)
    }

    fun observeRefreshToken(activity: BaseActivity): LiveData<String> {
        return LoginRepository().observeToken(activity)
    }


}