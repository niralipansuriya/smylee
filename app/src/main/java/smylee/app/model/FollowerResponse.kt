package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FollowerResponse
    (

    val follower_id: String,
    val other_user_id: String,
    val profile_name: String,
    val profile_pic: String,
    val is_following: Int,
    val mark_as_verified_badge: Int
) : Parcelable