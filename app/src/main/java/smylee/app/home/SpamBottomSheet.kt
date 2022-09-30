package smylee.app.home

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_for_you_fragment.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.listener.OnSpamDialogListener
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Logger
import smylee.app.utils.Utils

class SpamBottomSheet(private val onSpamDialogListener: OnSpamDialogListener): BottomSheetDialogFragment() {

    lateinit var viewModel: HomeViewModel
    lateinit var activity: BaseActivity

    var userId: String = ""
    private var commentId: String = ""
    private var postId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_spam_layout, container)
        viewModel = ViewModelProviders.of(activity).get(HomeViewModel::class.java)

        if(arguments != null) {
            userId = arguments!!.getString("userId", "")!!
            commentId = arguments!!.getString("commentId", "")
            postId = arguments!!.getInt("postId")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnReport.setOnClickListener {
            if (!rb_1.isChecked && !rb_2.isChecked && !rb_3.isChecked && !rb_4.isChecked && !rb_5.isChecked && !rb_6.isChecked) {
                Utils.showToastMessage(activity, resources.getString(R.string.validateSpam))
            } else {
                when {
                    rb_1.isChecked -> {
                        flagVideo(postId.toString(), "1", rb_1.text.toString(), commentId)
                    }
                    rb_2.isChecked -> {
                        flagVideo(postId.toString(), "2", rb_2.text.toString(), commentId)
                    }
                    rb_3.isChecked -> {
                        flagVideo(postId.toString(), "3", rb_3.text.toString(), commentId)
                    }
                    rb_4.isChecked -> {
                        flagVideo(postId.toString(), "4", rb_4.text.toString(), commentId)
                    }
                    rb_5.isChecked -> {
                        flagVideo(postId.toString(), "5", rb_5.text.toString(), commentId)
                    }
                    rb_6.isChecked -> {
                        flagVideo(postId.toString(), "6", rb_6.text.toString(), commentId)
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog: BottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val bottomSheet: FrameLayout = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.print("CommentBottomSheet onCancel")
        onSpamDialogListener.onSpamDialogDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Logger.print("CommentBottomSheet onDismiss")
        onSpamDialogListener.onSpamDialogDismiss()
    }

    private fun flagVideo(post_Id: String, spam_id: String, spam_text: String, commentID: String) {
        val hashMap = HashMap<String, String>()
        hashMap["post_id"] = post_Id
        hashMap["spam_id"] = spam_id
        hashMap["spam_text"] = spam_text
        if (!commentID.contentEquals("")) {
            hashMap["comment_or_post"] = "1"
        } else {
            hashMap["comment_or_post"] = "0"
        }
        if (!commentID.contentEquals("")) {
            hashMap["comment_id"] = commentID
        }

        viewModel.FlageVideo(activity, hashMap, true).observe(this, Observer {
            if (it != null) {
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    clearReportSelection()
                    commentId = ""
                    Utils.showToastMessage(activity, jsonObject["message"].toString())
                    dismiss()
                } else if (code == 0) {
                    clearReportSelection()
                    commentId = ""
                    Utils.showToastMessage(activity, jsonObject["message"].toString())
                    dismiss()
                }
            }
        })
    }

    private fun clearReportSelection() {
        rgReport.clearCheck()
    }

    companion object {
        fun newInstance(userId: String, commentId: String, postId: Int,
                        onSpamDialogListener: OnSpamDialogListener): SpamBottomSheet {
            val args = Bundle()
            args.putString("userId", userId)
            args.putString("commentId", commentId)
            args.putInt("postId", postId)
            val fragment = SpamBottomSheet(onSpamDialogListener)
            fragment.arguments = args
            return fragment
        }
    }
}