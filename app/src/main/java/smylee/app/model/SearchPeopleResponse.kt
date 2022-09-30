package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchPeopleResponse
    (
    val profile_name: String?,
    val user_name: String?,
    val profile_pic: String?,
    val user_id: String?,
    val is_following: Int?,
    val mark_as_verified_badge: Int?

) : Parcelable