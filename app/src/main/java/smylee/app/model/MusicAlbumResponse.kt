package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import smylee.app.model.AlbumForYouResponse

@Parcelize
data class MusicAlbumResponse(
    val album_id: String,
    val album_name: String,
    val audio_list: ArrayList<AlbumForYouResponse>?
) : Parcelable
