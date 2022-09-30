package smylee.app.discovercategory

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
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
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.utils.Logger
import smylee.app.utils.Methods
import java.text.DecimalFormat

//class DebatesAdapter(context: Context, categoryVideo: ArrayList<CategoryVideo>?,USER_ID:String) :
class DebatesAdapter(
    context: Context,
    categoryVideo: ArrayList<ForYouResponse>?,
    USER_ID: String
) :
    RecyclerView.Adapter<DebatesAdapter.ViewHolder>() {


    private var context: Context? = null

    private var USER_ID: String = ""
    private var categoryVideo: ArrayList<ForYouResponse>? = null
    private var width: Int = 0
    private var height: Int = 0

    init {
        this.context = context
        this.categoryVideo = categoryVideo
        this.USER_ID = USER_ID
        this.width = dpToPx(context.resources?.getDimensionPixelSize(R.dimen._120sdp)!!)
        this.height = dpToPx(context.resources?.getDimensionPixelSize(R.dimen._120sdp)!!)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_debates_layout, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        width = context?.resources?.getDimensionPixelSize(R.dimen._150sdp)!!
        holder.bindItems(
            position,
            categoryVideo,
            context,
            USER_ID,
            width,
            height
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

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvpostCount = itemView.findViewById(R.id.tvpostCount) as AppCompatTextView
        val mVideoView = itemView.findViewById(R.id.mVideoView) as AppCompatImageView
        val mPlayVideo = itemView.findViewById(R.id.mPlayVideo) as AppCompatImageView
        val tv_duration = itemView.findViewById(R.id.tv_duration) as AppCompatTextView

        fun bindItems(
            pos: Int,
            categoryVideo: ArrayList<ForYouResponse>?,
            context: Context?,
            USER_ID: String,
            width: Int,

            height: Int
        ) {
            var video_duration: String = categoryVideo!![pos].post_video_duration.toString()

            if (video_duration != null) {
                if (video_duration.contains(".")) {
                    val separated: List<String> = video_duration.split(".")
                    Log.d("separated one=========", separated[0])
                    Log.d("separated two=========", separated[1])

                    if (separated[1].length > 1) {
                        Logger.print("separated size greter !!!!!!!!" + separated.size)
                        video_duration = separated[0] + ":" + separated[1]
                    } else {
                        Logger.print("separated size not grater!!!!!!!!" + separated.size)
                        video_duration = separated[0] + ":" + separated[1] + "0"
                    }
                } else {
                    video_duration = "$video_duration:00"
                }
                tv_duration.text = video_duration
            }

            if (categoryVideo[pos].post_view_count != null && categoryVideo[pos].post_view_count != "") {
                Methods.showViewCounts(categoryVideo[pos].post_view_count!!.toString(), tvpostCount)

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
                                /*val factor = width / resource.width.toFloat()
                                val bitmap = Bitmap.createScaledBitmap(resource, width, (resource.height * factor).toInt(), true)!!*/
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


            /*mVideoView.setOnClickListener {
                mVideoView.isEnabled = false
                Handler().postDelayed({ // This method will be executed once the timer is over
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
                context!!.startActivity(intent)
            }*/
            mVideoView.setOnClickListener {
                mVideoView.isEnabled = false
                Handler().postDelayed({ // This method will be executed once the timer is over
                    mVideoView.isEnabled = true
                }, 2000)

                val intent = Intent(context, VideoDetailActivity::class.java)
                intent.putExtra("position", pos)
                intent.putExtra("cat_ID", categoryVideo[pos].category_id.toString())
                intent.putExtra("screen", "discover")
                intent.putExtra("hashtag", "")
                intent.putExtra("responseData", categoryVideo)
                intent.putExtra("pageNo", 1)
                intent.putExtra("searchTxt", "")
                intent.putExtra("userIDApi", "")

                context!!.startActivity(intent)
            }

        }


    }
}