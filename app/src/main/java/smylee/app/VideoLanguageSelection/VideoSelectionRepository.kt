package smylee.app.VideoLanguageSelection

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class VideoSelectionRepository : BaseRepository() {

    fun observeVideoSelectionLanguage(activity: BaseActivity, reqHashMap: HashMap<String, String>): MutableLiveData<String> {
        return callAPI(
            APIConstants.VIDEOLANGUAGE, activity, APIConstants.POST_API,
            reqHashMap, true,false, null)
    }

    fun observeVideoLanguage(activity: BaseActivity, reqHashMap: HashMap<String, String>): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEVIDEOLANGUAGE, activity, APIConstants.POST_API,
            reqHashMap, true,false, null)
    }
}