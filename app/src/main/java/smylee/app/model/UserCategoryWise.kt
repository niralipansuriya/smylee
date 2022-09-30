package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCategoryWise(
    val category_name: String?,
    val category_count: String?,
    //val category_video_list: ArrayList<CategoryVideo>
    val category_video_list: ArrayList<ForYouResponse>
) : Parcelable