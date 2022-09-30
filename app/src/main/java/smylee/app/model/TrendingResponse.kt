package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import smylee.app.model.ForYouResponse

@Parcelize
data class TrendingResponse(

    val hashTagName: String?,
    val hashTagCount: String?,
    //val category_video_list: ArrayList<CategoryVideo>
    val video_list: ArrayList<ForYouResponse>

) : Parcelable