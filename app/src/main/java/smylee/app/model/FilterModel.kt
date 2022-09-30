package smylee.app.model

import smylee.app.camerfilters.FilterType

data class FilterModel(val filterType: FilterType, val filterName: String, val drawableId: Int) {
}