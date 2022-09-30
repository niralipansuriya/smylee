package smylee.app.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment

open class BaseFragment_x : Fragment() {

    private var baseActivity: BaseActivity? = null
    var base: Base_UrfeedFragement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = activity as BaseActivity
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (baseActivity == null) {
            baseActivity = activity as BaseActivity
        }
    }
}