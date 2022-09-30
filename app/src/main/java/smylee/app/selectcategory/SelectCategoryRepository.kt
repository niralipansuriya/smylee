package smylee.app.selectcategory

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class SelectCategoryRepository : BaseRepository() {
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

    fun observVideoSelectionLanguage(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.VIDEOLANGUAGE,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false, null)
    }

    fun observeUpdatecategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.UPDATEUSERCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, true,
            false,null)
    }

    fun observeHashTag(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.HASHTAGLIST,
            activity, APIConstants.POST_API,
            reqHashMap, false,
            false,null)
    }
}