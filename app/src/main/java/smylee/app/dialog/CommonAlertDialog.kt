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

import smylee.app.R
import smylee.app.databinding.CommonAlertDialogBinding

abstract class CommonAlertDialog(context: Context, theme: Int) : Dialog(context) {

    //  private var activity: AppCompatActivity? = null
    private var activity: FragmentActivity? = null

    private var commonAlertDialogBinding: CommonAlertDialogBinding? = null

    init {
        //  activity = context as AppCompatActivity
        activity = context as FragmentActivity
    }

    fun initDialog(title: String, message: String): CommonAlertDialog {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        commonAlertDialogBinding = activity?.layoutInflater?.let {
            DataBindingUtil.inflate(it, R.layout.common_alert_dialog, null, true)
        }
        commonAlertDialogBinding?.root?.let { setContentView(it) }
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        commonAlertDialogBinding?.tvMsg?.text = message
        commonAlertDialogBinding?.tvCancel?.setOnClickListener {
            dismiss()
            cancelClicked()
        }

        commonAlertDialogBinding?.tvBlock?.text = title
        commonAlertDialogBinding?.tvBlock?.setOnClickListener {
            dismiss()
            okClicked()

        }



        return this
    }


    abstract fun okClicked()
    abstract fun cancelClicked()
}