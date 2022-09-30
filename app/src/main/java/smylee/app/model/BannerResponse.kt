package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BannerResponse(
    val banner_id: Int?,
    val banner_image: String?,
    val tag_name: String?,
    val tag_id: String?
) : Parcelable