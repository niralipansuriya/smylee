package smylee.app.signup

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {
    fun observerSignupcall(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SignUpRepository().observeSignUpCall(activity, reqHashMap)
    }

    /*fun observeRefreshToken(activity: BaseActivity): LiveData<String> {
        return SignUpRepository().observeToken(activity)
    }*/
}