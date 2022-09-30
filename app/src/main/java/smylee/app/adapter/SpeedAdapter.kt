package smylee.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import smylee.app.R
import smylee.app.ui.Activity.CameraVideoRecording
import java.util.ArrayList

class SpeedAdapter(private val ctx: Context, private val audioFiles: ArrayList<String>, val manageClick: ManageClick) : BaseAdapter() {

    interface ManageClick {
        fun manageRadioClick(IDp: String?)
    }

    class ViewHolder {
        var chCheck: CheckBox? = null
    }
    private var viewHolder: ViewHolder? = null

    override fun getCount(): Int {
        return audioFiles.size
    }

    override fun getItem(i: Int): Any? {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val mInflater = LayoutInflater.from(ctx)
        val containView: View
        if (view == null) {
            containView = mInflater.inflate(R.layout.audio_dialog, viewGroup, false)
            viewHolder = ViewHolder()
            initView(containView)
            containView.tag = viewHolder
        } else {
            containView = view
            viewHolder = containView.tag as ViewHolder
        }
        viewHolder!!.chCheck!!.text = audioFiles[i]
        viewHolder!!.chCheck!!.setOnClickListener {
            manageClick.manageRadioClick(audioFiles[i])
        }
        println("IDS IN GETVIEW ${CameraVideoRecording.IDS_speed}")
        viewHolder!!.chCheck!!.isChecked = CameraVideoRecording.IDS_speed.contains(audioFiles[i])
        return containView
    }

    private fun initView(itemView: View) {
        viewHolder!!.chCheck = itemView.findViewById<View>(R.id.ch_check) as CheckBox
    }
}