package smylee.app.discovercategory

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class DiscoverViewModel : ViewModel() {

    fun getdiscovercategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return DiscoverRepository().observerdiscovercategory(
            activity,
            reqHashMap,
            isProgressShow
        )
    }

    fun getDiscoverDetails(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return DiscoverRepository().observeDiscoverDetails(
            activity,
            reqHashMap,
            isProgressShow
        )
    }

    fun getBanner(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return DiscoverRepository().observeBanner(activity, reqHashMap, isProgressShow)
    }

}