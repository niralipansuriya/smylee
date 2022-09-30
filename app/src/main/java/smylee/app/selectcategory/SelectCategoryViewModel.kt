package smylee.app.selectcategory

import smylee.app.ui.base.BaseActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SelectCategoryViewModel : ViewModel() {
    fun getCategoryForUser(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SelectCategoryRepository().observeusercategory(activity, reqHashMap)
    }

    fun getVideoSelectionlanguage(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SelectCategoryRepository().observVideoSelectionLanguage(activity, reqHashMap)
    }

    fun getUpdatedUserCategory(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SelectCategoryRepository().observeUpdatecategory(activity, reqHashMap)
    }

    fun getHashTagList(
        activity: BaseActivity,
        reqHashMap: HashMap<String, String>
    ): LiveData<String> {
        return SelectCategoryRepository().observeHashTag(activity, reqHashMap)
    }


}