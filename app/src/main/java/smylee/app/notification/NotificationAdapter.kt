package smylee.app.notification

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.utils.Logger
import smylee.app.R
import smylee.app.model.NotificationResponse
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    val context: Context,
    private val notificationList: ArrayList<NotificationResponse>,
    val USER_ID: String
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_notification_layout, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(notificationList, position, context, USER_ID)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        Logger.print("notificationList NotificationAdapter========" + notificationList.size)
        return notificationList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            notificationList: ArrayList<NotificationResponse>,
            position: Int,
            context: Context,
            USER_ID: String
        ) {
            val tv_name = itemView.findViewById(R.id.tv_name) as TextView
            val llNotification = itemView.findViewById(R.id.llNotification) as ConstraintLayout
            llNotification.setOnClickListener {
                val intent = Intent(context, NotificationDetailActivity::class.java)
                intent.putExtra("postID", notificationList[position].notify_id)
                intent.putExtra(
                    "notification_type",
                    notificationList[position].notification_type.toString()
                )
                context.startActivity(intent)

            }


            val tv_time = itemView.findViewById(R.id.tv_time) as TextView
            val tv_active = itemView.findViewById(R.id.tv_active) as TextView
            val iv_pic = itemView.findViewById(R.id.iv_pic) as RoundedImageView

            if (notificationList.size > 0) {
                // var notification_time = notificationList[position].created_date
                var profile_name = notificationList[position].notification_title


                if (profile_name!!.contains("{{profileName}}")) {
                    profile_name = profile_name.replace(
                        "{{profileName}}",
                        notificationList[position].profile_name.toString()
                    )

                    val sb = SpannableStringBuilder(profile_name)
                    val bss = StyleSpan(Typeface.BOLD) // Span to make text bold
                    val iss = StyleSpan(Typeface.NORMAL) //Span to make text italic
                    sb.setSpan(
                        bss,
                        0,
                        notificationList[position].profile_name?.length!!,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    ) // make first 4 characters Bold
                    sb.setSpan(
                        iss,
                        notificationList[position].profile_name?.length!! + 1,
                        profile_name.length,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    ) // make last 2 characters Italic
                    tv_name.text = sb
//                    tv_name.text = profile_name
                }
                val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                //tv_time.text = simpleDateFormat.format(notificationList[position].created_date!! * 1000)
                tv_time.text =
                    getDisplayableTime(notificationList[position].created_date!! * 1000, tv_time)


                val unix_seconds: Long = notificationList[position].created_date!!

                tv_active.setText(notificationList[position].notification_text)
                Glide.with(context)
                    .load(notificationList[position].profile_pic)
                    .error(R.drawable.profile_thumb)
                    .into(iv_pic)

                if (!USER_ID.contentEquals(notificationList[position].user_id.toString())) {
                    iv_pic.setOnClickListener {
                        val intent = Intent(context, OtherUserProfileActivity::class.java)
                        intent.putExtra("OTHER_USER_ID", notificationList[position].user_id)
                        context.startActivity(intent)
                    }
                }


            }
        }

        fun getDisplayableTime(delta: Long, time: TextView): String? {
            var difference: Long = 0
            val mDate = System.currentTimeMillis()
            if (mDate > delta) {
                difference = mDate - delta
                val seconds = difference / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                val months = days / 31
                val years = days / 365
                return if (seconds < 0) {
                    "not yet"
                } else if (seconds < 60) {
                    when (seconds) {
                        1L -> "just now"
                        0L -> "just now"
                        else -> "$seconds seconds ago"
                    }
                } else if (seconds < 120) {
                    "a minute ago"
                } else if (seconds < 2700) // 45 * 60
                {
                    "$minutes minutes ago"
                } else if (seconds < 5400) // 90 * 60
                {
                    "an hour ago"
                } else if (seconds < 86400) // 24 * 60 * 60
                {
                    "$hours hours ago"
                } else if (seconds < 172800) // 48 * 60 * 60
                {
                    "yesterday"
                } else if (seconds < 2592000) // 30 * 24 * 60 * 60
                {
                    "$days days ago"
                } else if (seconds < 31104000) // 12 * 30 * 24 * 60 * 60
                {
                    //  if (months <= 1) "one month ago" else "$days months ago"
                    if (months <= 1) "one month ago" else "$months months ago"
                } else {
                    if (years <= 1) "one year ago" else "$years years ago"
                }
            }
            return null
        }

    }
}