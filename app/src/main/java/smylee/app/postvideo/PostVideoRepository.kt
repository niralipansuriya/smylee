package smylee.app.postvideo

import smylee.app.ui.base.BaseActivity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import com.urfeed.listener.OnResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*

class PostVideoRepository : BaseRepository() {
    fun observePostVideo(
        activity: BaseActivity, hashMap: HashMap<String, RequestBody>
        , multiBody: List<MultipartBody.Part>?, multiBodyother: List<MultipartBody.Part>?
    ): MutableLiveData<String> {
        return callMultiPartAPIVideo(
            APIConstants.POSTVIDEOTOUSER, activity, hashMap,
            APIConstants.POSTVIDEOTOUSER, true, false,
            APIConstants.POST_API, multiBody, multiBodyother
        )
    }

    fun postVideoWithCallback(activity: Context, hashMap: HashMap<String, RequestBody>, multiBody: List<MultipartBody.Part>?,
        multiBodyother: List<MultipartBody.Part>?, onResponse: OnResponse) {
        callMultiPartAPIVideo(
            APIConstants.POSTVIDEOTOUSER, activity, hashMap, APIConstants.POSTVIDEOTOUSER,
            APIConstants.POST_API, multiBody, multiBodyother, onResponse)
    }

    fun observeVideoSelectionLanguage(activity: BaseActivity, reqHashMap: HashMap<String, String>): MutableLiveData<String> {
        return callAPI(APIConstants.VIDEOLANGUAGE, activity, APIConstants.POST_API, reqHashMap, true, false,null)
    }

    fun observeusercategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.USERCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }

    /*fun observeUpdatecategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        API: String
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.UPDATEUSERCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,
            null
        )

    }*/
}