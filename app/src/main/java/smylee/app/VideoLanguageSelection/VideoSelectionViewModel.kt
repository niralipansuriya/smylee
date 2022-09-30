package smylee.app.VideoLanguageSelection

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class VideoSelectionViewModel : ViewModel() {
    fun getVideoSelectionLanguage(activity: BaseActivity, reqHashMap: HashMap<String, String>): LiveData<String> {
        return VideoSelectionRepository().observeVideoSelectionLanguage(activity, reqHashMap)
    }

    fun getUpdatedUserLanguage(activity: BaseActivity, reqHashMap: HashMap<String, String>): LiveData<String> {
        return VideoSelectionRepository().observeVideoLanguage(activity, reqHashMap)
    }
}