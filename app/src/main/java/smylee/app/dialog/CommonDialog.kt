package smylee.app.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import smylee.app.databinding.DialogForAlertBinding
import smylee.app.R

abstract class CommonDialog(context: Context, theme: Int) : Dialog(context) {

    // private var activity: AppCompatActivity? = null
    private var activity: FragmentActivity? = null
    private var dialogForAlertBinding: DialogForAlertBinding? = null


    init {
        //activity = context as AppCompatActivity
        activity = context as FragmentActivity
    }

    fun initDialog(title: String, message: String): CommonDialog {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        dialogForAlertBinding = activity?.layoutInflater?.let {
            DataBindingUtil.inflate(it, R.layout.dialog_for_alert, null, true)
        }
        dialogForAlertBinding?.root?.let { setContentView(it) }
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogForAlertBinding?.tvMessage?.text = message

        dialogForAlertBinding?.tvOk?.setOnClickListener {
            dismiss()
            okClicked()
        }

        return this
    }


    abstract fun okClicked()

}
