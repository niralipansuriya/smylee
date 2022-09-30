package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Videolanguageresponse(
    var language_id: String?,
    var language_name: String?,
    var is_selected: Int?

) : Parcelable

