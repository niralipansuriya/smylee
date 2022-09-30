package smylee.app.signup

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class SignUpRepository : BaseRepository() {

    fun observeSignUpCall(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.signup, activity, APIConstants.POST_API, reqHashMap, true,
            false, null
        )
    }

    /*fun observeToken(activity: BaseActivity): MutableLiveData<String> {
        return callAPIRefreshToken(
            APIConstants.REFRESH_TOKEN_API,
            activity, APIConstants.POST_API, true,
            false, null
        )
    }*/
}