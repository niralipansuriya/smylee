package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlbumForYouResponse(
    val audio_id: String,
    val audio_title: String,
    val audio_description: String,
    val audio_pic: String,
    val audio_uplink: String,
    var viewType: Int,
    var isPlaying: Int = 0
) : Parcelable