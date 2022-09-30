package smylee.app.trending

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import smylee.app.utils.Logger
import smylee.app.utils.Methods

class TrendingFinalAdapter(
    context: Context,
    categoryVideo: ArrayList<ForYouResponse>?,
    USER_ID: String,
    followerMap: HashMap<String, String>,
    likeVideoHash: HashMap<String, String>,
    hasLiked: HashMap<String, String>,
    hashTag: String, val manageClick: ManageClick
) :
    RecyclerView.Adapter<TrendingFinalAdapter.ViewHolder>() {

    var followerMap: HashMap<String, String> = HashMap()
    var likeVideoHash: HashMap<String, String> = HashMap()
    private var hasLiked: HashMap<String, String> = HashMap()

    private var context: Context? = null

    private var userId: String = ""
    private var hashTag: String = ""
    private var categoryVideo: ArrayList<ForYouResponse>? = null
    private var width: Int = 0
    private var height: Int = 0

    init {
        this.context = context
        this.categoryVideo = categoryVideo
        this.userId = USER_ID
        this.followerMap = followerMap
        this.likeVideoHash = likeVideoHash
        this.hasLiked = hasLiked
        this.width = dpToPx(context.resources?.getDimensionPixelSize(R.dimen._120sdp)!!)
        this.height = dpToPx(context.resources?.getDimensionPixelSize(R.dimen._120sdp)!!)
        this.hashTag = hashTag
    }

    interface ManageClick {
        fun managePlayVideoClick(position: Int?)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_trending_video_list, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mVideoView.setOnClickListener {
            holder.mVideoView.isEnabled = false
            Handler().postDelayed({
                holder.mVideoView.isEnabled = true
            }, 2000)
            manageClick.managePlayVideoClick(position)
        }
        holder.bindItems(
            position,
            categoryVideo,
            context,
            followerMap,
            likeVideoHash,
            hasLiked,
            hashTag
        )
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return categoryVideo!!.size
    }

    private fun dpToPx(dp: Int): Int {
        val density: Int = context?.resources?.displayMetrics?.density!!.toInt()
        return (dp * density)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPostCount = itemView.findViewById(R.id.tvpostCount) as AppCompatTextView
        val mVideoView = itemView.findViewById(R.id.mVideoView) as AppCompatImageView
        val mPlayVideo = itemView.findViewById(R.id.mPlayVideo) as AppCompatImageView
        private val tvDuration = itemView.findViewById(R.id.tv_duration) as AppCompatTextView

        fun bindItems(
            pos: Int,
            categoryVideo: ArrayList<ForYouResponse>?,
            context: Context?,
            followerMap: HashMap<String, String>,
            likeVideoHash: HashMap<String, String>,
            hasLiked: HashMap<String, String>,
            hashTag: String
        ) {
            var videoDuration: String = categoryVideo!![pos].post_video_duration.toString()
            videoDuration = if (videoDuration.contains(".")) {
                val separated: List<String> = videoDuration.split(".")
                if (separated[1].length > 1) {
                    Logger.print("separated size greter !!!!!!!!" + separated.size)
                    separated[0] + ":" + separated[1]
                } else {
                    Logger.print("separated size not grater!!!!!!!!" + separated.size)
                    separated[0] + ":" + separated[1] + "0"
                }
            } else {
                "$videoDuration:00"
            }
            tvDuration.text = videoDuration

            if (categoryVideo[pos].post_view_count != null && categoryVideo[pos].post_view_count != "") {
                Methods.showViewCounts(categoryVideo[pos].post_view_count!!.toString(), tvPostCount)
            }
            if (categoryVideo[pos].post_video_thumbnail_compres!!.contains("GIF") || categoryVideo[pos].post_video_thumbnail_compres!!.contains(
                    "gif"
                )
            ) {
                mPlayVideo.visibility = View.GONE
                if (categoryVideo[pos].post_height != null && categoryVideo[pos].post_height != null) {

                    Glide.with(context!!)
                        .asGif()
                        .error(R.drawable.thumb)
                        .placeholder(R.drawable.thumb)
                        .load(categoryVideo[pos].post_video_thumbnail_compres)
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
                                if (categoryVideo[pos].post_height!! > categoryVideo[pos].post_width!!) {
                                    mVideoView.scaleType = ImageView.ScaleType.CENTER_CROP

                                } else {
                                    mVideoView.scaleType = ImageView.ScaleType.FIT_CENTER

                                }
                                return false

                            }


                        })
                        .into(mVideoView)
                }
            } else {
                mPlayVideo.visibility = View.VISIBLE

                Glide.with(context!!)
                    .asBitmap()
                    .error(R.drawable.thumb)
                    .placeholder(R.drawable.thumb)
                    .load(categoryVideo[pos].post_video_thumbnail_compres)
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


            /*  mVideoView.setOnClickListener {
                  mVideoView.isEnabled = false
                  Handler().postDelayed({
                      mVideoView.isEnabled = true
                  }, 2000)
                  val intent = Intent(context, VideoDetailActivity::class.java)
                  intent.putExtra("position", pos)
                  intent.putExtra("cat_ID", "")
                  intent.putExtra("screen", "trending")
                  intent.putExtra("hashtag", hashTag)
                  intent.putExtra("searchTxt", "")
                  intent.putExtra("userIDApi", "")
                  intent.putExtra("responseData", categoryVideo)
                  intent.putExtra("pageNo", 1)
                  context.startActivity(intent)
              }*/
            /*   mVideoView.setOnClickListener {
                   mVideoView.isEnabled = false
                   Handler().postDelayed({
                       mVideoView.isEnabled = true
                   }, 2000)
                   val intent = Intent(context, VideoPlayActivity::class.java)
                   intent.putExtra("response", categoryVideo)
                   intent.putExtra("position", pos)
                   intent.putExtra("cat_ID", categoryVideo[pos].category_id.toString())
                   intent.putExtra("is_discover", true)
                   intent.putExtra("isProfile", false)
                   intent.putExtra("followerMap", followerMap)
                   intent.putExtra("likeCountMap", likeVideoHash)
                   intent.putExtra("hasLikedMap", hasLiked)
                   context.startActivity(intent)
               }*/
        }
    }
}