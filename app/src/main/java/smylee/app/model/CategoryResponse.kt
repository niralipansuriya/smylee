package smylee.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize

data class CategoryResponse
    (
    var category_name: String?,
    var category_id: String?,
    var category_icon: String?,
    var category_color_code: String?,
    var is_selected: Int?

) : Parcelable
