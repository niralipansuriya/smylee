package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.MutableLiveData
import smylee.app.retrofitclient.APIConstants
import smylee.app.ui.base.BaseRepository
import java.util.HashMap

class MusicListingRepository : BaseRepository() {

    fun observeAudioListingForYou(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {
        return callAPI(
            APIConstants.FORYOUALBUMLIST,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun observeAlbumAudioList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.ALBUMLISTAUDIO,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }

    fun observeAlbumAudioListingDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>,
        isProgressShow: Boolean
    ): MutableLiveData<String> {

        return callAPI(
            APIConstants.ALBUMAUDIODETAIL,
            activity, APIConstants.POST_API,
            reqHashMap, isProgressShow,
            false,null)
    }
}