package smylee.app.Profile

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
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
import smylee.app.R
import smylee.app.model.ForYouResponse
import smylee.app.utils.Methods


//class OtherProfileAdapter(context: Context, list: ArrayList<ProfileResponse>?, val manageClick: ManageClick
class OtherProfileAdapter(context: Context, list: ArrayList<ForYouResponse>?, val manageClick: ManageClick
) : RecyclerView.Adapter<OtherProfileAdapter.ViewHolder>() {

    private var context: Context? = null
    private var list: ArrayList<ForYouResponse>? = null
    private var width: Int = 0
//    private var hasLikedMap: HashMap<String, String> = HashMap()
    var likeVideoHash: HashMap<String, String> = HashMap()

    init {
        this.context = context
        this.list = list
    }

    interface ManageClick {
        fun manageDeletePost(IDp: String?)
        fun managePlayVideoClick(position: Int)
        fun shareVideo(position: Int, url: String, postHeight: Int, podtWidth: Int)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_profile_post_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        width = dpToPx(context?.resources?.getDimensionPixelSize(R.dimen._150sdp)!!)
        holder.mVideoView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.thumb))
        holder.mVideoView.setOnClickListener {
            holder.mVideoView.isEnabled = false
            Handler().postDelayed({ // This method will be executed once the timer is over
                holder.mVideoView.isEnabled = true
            }, 2000)

            manageClick.managePlayVideoClick(position)
        }
        holder.ivShare.setOnClickListener {
            manageClick.shareVideo(
                position,
                list!![position].post_video!!,
                list!![position].post_height!!,
                list!![position].post_width!!
            )
        }
        holder.bindItems(position, list, context!!)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    private fun dpToPx(dp: Int): Int {
        val density: Int = context?.resources?.displayMetrics?.density!!.toInt()
        return (dp * density)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return list!!.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVideoView = itemView.findViewById(R.id.mVideoView) as AppCompatImageView
        val mPlayVideo = itemView.findViewById(R.id.mPlayVideo) as AppCompatImageView
        val ivShare = itemView.findViewById(R.id.ivShare) as AppCompatImageView
        fun bindItems(pos: Int, list: ArrayList<ForYouResponse>?, context: Context) {
            val tvPostCount = itemView.findViewById(R.id.tvpostCount) as AppCompatTextView
            val txtVideoDuration =
                itemView.findViewById(R.id.txt_videoDuration) as AppCompatTextView

            tvPostCount.setOnClickListener {}

            if (list!![pos].post_video_thumbnail_compres!!.contains("GIF") || list[pos].post_video_thumbnail_compres!!.contains(
                    "gif"
                )
            ) {
                mPlayVideo.visibility = View.GONE
                if (list!![pos].post_height != null && list[pos].post_height != null) {

                    Glide.with(context)
                        .asGif()
                        .error(R.drawable.thumb)
                        .placeholder(R.drawable.thumb)
                        .load(list[pos].post_video_thumbnail_compres)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<GifDrawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<GifDrawable?>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP
                                return false
                            }

                            override fun onResourceReady(
                                resource: GifDrawable?,
                                model: Any?,
                                target: Target<GifDrawable?>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (list.size > 0) {
                                    if (list[pos].post_height!! > list[pos].post_width!!) {
                                        mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP

                                    } else {
                                        mVideoView.scaleType = ImageView.ScaleType.FIT_CENTER

                                    }
                                }


                                return false

                            }


                        })

                        .into(mVideoView)
                }
            } else {
                mPlayVideo.visibility = View.VISIBLE

                Glide.with(context)
                    .asBitmap()
                    .error(R.drawable.thumb)
                    .placeholder(R.drawable.thumb)
                    .load(list!![pos].post_video_thumbnail_compres)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            if (resource.height > resource.width) {
                                mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP
                                mVideoView.setImageBitmap(resource)
                            } else {
                                val layoutParams = mVideoView.layoutParams
                                layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                                mVideoView.scaleType = ImageView.ScaleType.FIT_CENTER
                                mVideoView.setImageBitmap(resource)
                            }
                        }
                    })
            }


            var videoDuration: String = list[pos].post_video_duration.toString() + ""
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
            txtVideoDuration.text = videoDuration

            if (list[pos].post_view_count != null && list[pos].post_view_count != "") {
                Methods.showViewCounts(list[pos].post_view_count!!.toString(), tvPostCount)
            }
        }
    }
}