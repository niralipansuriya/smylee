package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MusicListingViewModel : ViewModel() {

    fun observeAudioListinfgForyou(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return MusicListingRepository().observeAudioListingForYou(
            activity,
            reqHashMap,
            isProgressShow
        )
    }

    fun observeAlbumAudioListing(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return MusicListingRepository().observeAlbumAudioList(
            activity,
            reqHashMap,
            isProgressShow
        )
    }

    fun observeAlbumAudioListingDetail(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>, isProgressShow: Boolean
    ): LiveData<String> {
        return MusicListingRepository().observeAlbumAudioListingDetail(
            activity,
            reqHashMap,
            isProgressShow
        )
    }

}