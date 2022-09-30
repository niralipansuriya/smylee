package smylee.app.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import smylee.app.R
import smylee.app.model.CommentResponse
import smylee.app.utils.Logger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class BottomSheetAdapter(context: Context, Post_ID: Int?, userId: String, commentHasLikeMap: HashMap<String, String>,
    commentLikeMap: HashMap<String, String>, comments_list: ArrayList<CommentResponse>?, var manageClick: ManageClick,
    private var manageProfileClick: ManageClickProfile) : RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

    interface ManageClick {
        fun likeUnlike(IDp: Int?, like_counts: String?, has_liked: Int?, Post_ID: Int?, like_img: ImageView,
            tv_like_count: TextView, commentLikeCountsInitial: String, CommentHasLikeInitial: String, position: Int)
    }

    interface ManageClickProfile {
        fun clickUserProfile(userId: String)
        fun spamComment(commentId: String)
    }

    private var context: Context? = null
    private var postID: Int? = null
    private var userId: String? = ""
    private var commentHasLikeMap: HashMap<String, String> = HashMap()
    private var commentLikeMap: HashMap<String, String> = HashMap()
    private var commentsList: ArrayList<CommentResponse>? = null

    init {
        this.context = context
        this.commentsList = comments_list
        this.postID = Post_ID
        this.commentLikeMap = commentLikeMap
        this.commentHasLikeMap = commentHasLikeMap
        this.userId = userId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_bottom_sheet, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(context, commentsList, position, commentHasLikeMap, commentLikeMap)
        Logger.print("userId !!!!!!!!!!!!!!!!!$userId")
        Logger.print("other userId !!!!!!!!!!!!!!!!!" + commentsList!![position].user_id.toString())
        if (userId!!.contentEquals(commentsList!![position].user_id.toString())) {
            holder.ivReportComment.visibility = View.GONE
        } else {
            holder.ivReportComment.visibility = View.VISIBLE
        }

        holder.ivPic.setOnClickListener {
            holder.ivPic.isEnabled = false
            Handler().postDelayed({ // This method will be executed once the timer is over
                holder.ivPic.isEnabled = true
            }, 2000)
            manageProfileClick.clickUserProfile(commentsList!![position].user_id.toString())
        }

        holder.ivReportComment.setOnClickListener {
            manageProfileClick.spamComment(commentsList!![position].post_comment_id.toString())
        }

        holder.like.setOnClickListener {
            //  manageClick.likeUnlike(comments_list!!.get(position).post_comment_id,comments_list!!.get(position).comment_like_count,comments_list!!.get(position).has_liked,Post_ID,holder.like,holder.tv_like_count)
            manageClick.likeUnlike(commentsList!![position].post_comment_id, "", 0, postID,
                holder.like,holder.tvLikeCount,"","",position)
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return commentsList!!.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val like = itemView.findViewById(R.id.like) as ImageView
        val tvLikeCount = itemView.findViewById(R.id.tv_like_count) as TextView
        val ivPic = itemView.findViewById(R.id.iv_pic) as ImageView
        val ivReportComment = itemView.findViewById(R.id.ivReportComment) as ImageView

        fun bindItems(context: Context?, commentsList: ArrayList<CommentResponse>?, position: Int,
            commentHasLikeMap: HashMap<String, String>, commentLikeMap: HashMap<String, String>) {
            val tvUserName = itemView.findViewById(R.id.tv_user_name) as TextView
            tvUserName.text = commentsList!![position].profile_name
            val tvComment = itemView.findViewById(R.id.tv_comment) as TextView
            tvComment.text = commentsList[position].user_comment
            val tvTimeCmt = itemView.findViewById(R.id.tv_time_cmt) as TextView
            val ivVerified = itemView.findViewById(R.id.ivVerified) as AppCompatImageView

            if (commentsList[position].mark_as_verified_badge != null && commentsList[position].mark_as_verified_badge == 1) {
                ivVerified.visibility = View.VISIBLE
            } else {
                ivVerified.visibility = View.GONE

            }

            tvTimeCmt.text = getDisplayableTime(commentsList[position].created_date!! * 1000)
            Logger.print("HAS LIKED OR NOT!!!!!!!!!!!!!!" + commentsList[position].has_liked)
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

            Logger.print("COMMENT TIME!!!!!!!!!!!!!!" + simpleDateFormat.format(commentsList[position].created_date!! * 1000))

            if (commentsList[position].profile_pic != null && !commentsList[position].profile_pic!!.contentEquals(
                    ""
                )
            ) {
                ivPic.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(context!!)
                    .load(commentsList[position].profile_pic)
                    .error(R.drawable.profile_thumb)
                    .into(ivPic)
            } else {
                ivPic.scaleType = ImageView.ScaleType.CENTER_INSIDE
                ivPic.setImageResource(R.drawable.profile_thumb)
            }
            Logger.print("commentHasLikeMap in adapter==================$commentHasLikeMap")

            val commentHasLikeInitial: String = commentHasLikeMap[commentsList[position].post_comment_id.toString()].toString()
            Logger.print("HAS LIKED OR NOT IN ADAPTER*************$commentHasLikeInitial")
            if (commentHasLikeInitial.contentEquals("1")) {
                like.setImageResource(R.drawable.likecomment)
            } else {
                like.setImageResource(R.drawable.unlikecomment)
            }

            val commentLikeCountsInitial: String = commentLikeMap[commentsList[position].post_comment_id.toString()].toString()
            if (commentLikeCountsInitial.contentEquals("null")) {
                tvLikeCount.text = "0"
            } else {
                if (commentLikeCountsInitial != "") {
                    showCommentLikeCounts(commentLikeCountsInitial, tvLikeCount)
                }
            }
        }

        private fun showCommentLikeCounts(actual_view_counts: String, like_txt: TextView) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")
            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            like_txt.text = numberString1
        }

        private fun getDisplayableTime(delta: Long): String? {
            val difference: Long
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
                    Logger.print("not yet")
                    "not yet"
                } else if (seconds < 60) {
                    when (seconds) {
                        1L -> "just now"
                        0L -> "just now"
                        else -> "$seconds seconds ago"
                    }
                } else if (seconds < 120) {
                    "a minute ago"
                } else if (seconds < 2700) { // 45 * 60
                    "$minutes minutes ago"
                } else if (seconds < 5400) { // 90 * 60
                    "an hour ago"
                } else if (seconds < 86400) { // 24 * 60 * 60
                    "$hours hours ago"
                } else if (seconds < 172800) { // 48 * 60 * 60
                    "yesterday"
                } else if (seconds < 2592000) { // 30 * 24 * 60 * 60
                    "$days days ago"
                } else if (seconds < 31104000) { // 12 * 30 * 24 * 60 * 60
                    //if (months <= 1) "one month ago" else "$days months ago"
                    if (months <= 1) "one month ago" else "$months months ago"
                } else {
                    if (years <= 1) "one year ago" else "$years years ago"
                }
            } else {
                return "just now"
            }
        }
    }
}