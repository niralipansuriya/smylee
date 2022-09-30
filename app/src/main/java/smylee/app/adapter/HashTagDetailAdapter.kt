package smylee.app.discovercategory

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.makeramen.roundedimageview.RoundedImageView
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.R
import smylee.app.model.ForYouResponse
import smylee.app.utils.Methods

class HashTagDetailAdapter(
    context: Context,
    categoryVideo: ArrayList<ForYouResponse>,
    USER_ID: String,
    val manageClick: ManageClick
) : RecyclerView.Adapter<HashTagDetailAdapter.ViewHolder>() {

    private var context: Context? = null
    var userId: String = ""
    private var width: Int = 0

    private var categoryVideo: ArrayList<ForYouResponse>? = null

    init {
        this.context = context
        this.categoryVideo = categoryVideo
        this.userId = USER_ID

    }

    interface ManageClick {
        fun managePlayVideoClick(position: Int?)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_hash_tag_detail, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //  holder.mVideoView.setImageBitmap(null)
        holder.mVideoView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.thumb))

        holder.ivPic.setImageBitmap(null)


        holder.mVideoView.setOnClickListener {
            manageClick.managePlayVideoClick(position)
        }

        width = dpToPx(context?.resources?.getDimensionPixelSize(R.dimen._150sdp)!!)
        if (categoryVideo!![position].profile_pic != null && categoryVideo!![position].profile_pic != "") {
            holder.ivPic.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(context!!)
                .load(categoryVideo!![position].profile_pic)
                .error(R.drawable.userpicfinal)
                .into(holder.ivPic)
        } else {
            holder.ivPic.scaleType = ImageView.ScaleType.FIT_CENTER
            holder.ivPic.setImageResource(R.drawable.userpicfinal)
        }
        if (categoryVideo!![position].post_video_thumbnail_compres!!.contains("GIF") || categoryVideo!![position].post_video_thumbnail_compres!!.contains(
                "gif"
            )
        ) {
            holder.mPlayVideo.visibility = View.GONE

            Glide.with(context!!)
                .asGif()
                .error(R.drawable.thumb)
                .placeholder(R.drawable.thumb)
                .load(categoryVideo!![position].post_video_thumbnail_compres)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<GifDrawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (categoryVideo!![position].post_height!! > categoryVideo!![position].post_width!!) {
                            holder.mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP

                        } else {
                            holder.mVideoView.scaleType = ImageView.ScaleType.FIT_CENTER

                        }
                        return false

                    }


                })

                .into(holder.mVideoView)

        } else {
            holder.mPlayVideo.visibility = View.VISIBLE

            Glide.with(context!!)
                .asBitmap()
                .error(R.drawable.thumb)
                .placeholder(R.drawable.thumb)
                .load(categoryVideo!![position].post_video_thumbnail_compres)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
//                    Log.i("PlayerFragment","onNewResultImpl ${resource.width} * ${resource.height}")
                        if (resource.height > resource.width) {
//                        Log.i("PlayerFragment", "Width $width")
                            holder.mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP
                            holder.mVideoView.setImageBitmap(resource)
                        } else {
                            val layoutParams = holder.mVideoView.layoutParams
                            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                            holder.mVideoView.scaleType = ImageView.ScaleType.FIT_CENTER
                            holder.mVideoView.setImageBitmap(resource)
                        }
                    }
                })
        }

        holder.bindItems(position, categoryVideo, context, userId)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return categoryVideo!!.size
    }

    private fun dpToPx(dp: Int): Int {
        val density: Int = context?.resources?.displayMetrics?.density!!.toInt()
        return (dp * density)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVideoView = itemView.findViewById(R.id.mVideoView) as AppCompatImageView
        val ivPic = itemView.findViewById(R.id.iv_pic) as RoundedImageView
        val mPlayVideo = itemView.findViewById(R.id.mPlayVideo) as AppCompatImageView

        fun bindItems(
            pos: Int,
            categoryVideo: ArrayList<ForYouResponse>?,
            context: Context?,
            USER_ID: String
        ) {
            val txtTitle = itemView.findViewById(R.id.txt_title) as AppCompatTextView
            val tvDuration = itemView.findViewById(R.id.tv_duration) as AppCompatTextView
            val txtDesc = itemView.findViewById(R.id.txt_desc) as AppCompatTextView
            val tvPostCount = itemView.findViewById(R.id.tvpostCount) as AppCompatTextView

            if (categoryVideo != null && categoryVideo.size > 0) {
                var videoDuration: String = categoryVideo[pos].post_video_duration.toString()
                videoDuration = if (videoDuration.contains(".")) {
                    val separated: List<String> = videoDuration.split(".")
                    if (separated[1].length > 1) {
                        separated[0] + ":" + separated[1]
                    } else {
                        separated[0] + ":" + separated[1] + "0"
                    }
                } else {
                    "$videoDuration:00"
                }
                tvDuration.text = videoDuration
                if (categoryVideo[pos].post_view_count != null && categoryVideo[pos].post_view_count != "") {
                    Methods.showViewCounts(
                        categoryVideo[pos].post_view_count!!.toString(),
                        tvPostCount
                    )
                }

                /*  mVideoView.setOnClickListener {
                      val intent = Intent(context, VideoPlayActivity::class.java)
                      intent.putExtra("response", categoryVideo)
                      intent.putExtra("position", pos)
                      intent.putExtra("cat_ID", "")
                      intent.putExtra("is_discover", false)
                      intent.putExtra("isProfile", false)
                      intent.putExtra("followerMap", followerMap)
                      intent.putExtra("likeCountMap", likeVideoHash)
                      intent.putExtra("hasLikedMap", has_likedMap)
                      context!!.startActivity(intent)
                  }*/
                txtTitle.text = categoryVideo[pos].profile_name
                txtDesc.text = categoryVideo[pos].post_title

                if (!USER_ID.contentEquals(categoryVideo[pos].user_id.toString())) {
                    ivPic.setOnClickListener {
                        if (categoryVideo[pos].user_id != null) {
                            val intent = Intent(context, OtherUserProfileActivity::class.java)
                            intent.putExtra("OTHER_USER_ID", categoryVideo[pos].user_id.toString())
                            context!!.startActivity(intent)
                        }
                    }
                }
            }
        }

    }
}