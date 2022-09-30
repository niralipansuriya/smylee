package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlmunListResponse
    (

    val audio_id: String,
    val audio_title: String,
    val audio_description: String,
    val audio_pic: String,
    val audio_uplink: String
) : Parcelable