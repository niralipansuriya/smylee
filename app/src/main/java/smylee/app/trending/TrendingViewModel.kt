package smylee.app.trending

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class TrendingViewModel : ViewModel() {
    /*fun observeTrendingList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return TrendingRepository().observeTrendingList(activity, reqHashMap, isProgressShow)
    }*/

    fun observeHashTagTrendingList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return TrendingRepository().observeList(activity, reqHashMap, isProgressShow)
    }

    fun observeHashTagDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return TrendingRepository().observeHashTagDetail(activity, reqHashMap, isProgressShow)
    }
}