package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ForYouResponse(
    val post_id: Int?,
    val post_title: String?,
    val post_video: String?,
    val post_height: Int?,
    val post_width: Int?,
    val post_video_thumbnail: String?,
    val profile_pic: String?,
    val profile_pic_compres: String?,
    val mark_as_verified_badge: Int?,
    var post_comment_count: String?,
    var post_like_count: String?,
    val post_spam_count: String?,
    val post_view_count: String?,
    val profile_name: String?,
    val post_video_thumbnail_compres: String?,
    val post_type: Int?,
    val created_date: Int?,
    var is_following: Int?,
    val post_video_duration: Float?,
    val post_visibility: Int?,
    val category_id: Int?,
    val user_id: Int?,
    val category_color_code: String?,
    val category_name: String?,
    val no_of_followings: String?,
    val no_of_followers: String?,
    var has_liked: Int?,
    var isViewed: Boolean = false,
    var rowType: Int = 0

) : Parcelable