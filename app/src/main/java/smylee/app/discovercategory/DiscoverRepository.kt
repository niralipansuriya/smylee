package smylee.app.discovercategory

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class DiscoverRepository : BaseRepository() {
    fun observerdiscovercategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.DISCOVERCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }


    fun observeDiscoverDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.DISCOVERVIEWALLCATEGORY,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,
            null
        )
    }

    fun observeBanner(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.GETBANNER,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

}