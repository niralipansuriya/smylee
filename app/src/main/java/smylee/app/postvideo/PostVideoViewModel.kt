package smylee.app.postvideo

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.HashMap

class PostVideoViewModel : ViewModel() {
    fun observePostVideo(
        activity: BaseActivity,
        hashMap: HashMap<String, RequestBody>,
        multiBody: List<MultipartBody.Part>?,
        multiBody_other: List<MultipartBody.Part>?
    ): LiveData<String> {
        return PostVideoRepository().observePostVideo(activity, hashMap, multiBody, multiBody_other)
    }

    fun getCategoryForUser(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return PostVideoRepository().observeusercategory(activity, reqHashMap)
    }

    fun getVideoSelectionlanguage(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return PostVideoRepository().observeVideoSelectionLanguage(activity, reqHashMap)
    }

//    fun getUpdatedUserCategory(activity: smylee.app.ui.base.BaseActivity, reqHashMap: HashMap<String, String>, API : String): LiveData<String>
//    {
//        return PostVideoRepository().observeUpdatecategory(activity, reqHashMap, API)
//    }


}