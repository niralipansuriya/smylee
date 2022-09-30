package smylee.app.trending

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class TrendingRepository : BaseRepository() {
    /*fun observeTrendingList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FORYOULISTING,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }*/

    fun observeList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.TRENDINGLIST,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun observeHashTagDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.HASHTAGDETAIL,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

}