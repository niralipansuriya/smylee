package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationResponse(

    val notification_id: String?,
    val notification_type: Int?,
    val notification_text: String?,
    val notification_title: String?,
    val profile_name: String?,
    val notify_id: String?,
    val user_id: String?,
    val created_date: Long?,
    val profile_pic: String?

) : Parcelable