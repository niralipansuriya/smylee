package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HashTagResponse
    (

    var tag_name: String?,
    var tag_id: String?

) : Parcelable