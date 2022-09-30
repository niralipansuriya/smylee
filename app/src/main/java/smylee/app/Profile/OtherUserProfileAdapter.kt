package smylee.app.Profile

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
import smylee.app.model.ForYouResponse
import smylee.app.utils.Logger
import smylee.app.R
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.utils.Methods
import java.text.DecimalFormat

//class OtherUserProfileAdapter(context: Context, list: ArrayList<ProfileResponse>?) :
class OtherUserProfileAdapter(
    context: Context,
    list: ArrayList<ForYouResponse>?, val manageClick: ManageClick
) :
    RecyclerView.Adapter<OtherUserProfileAdapter.ViewHolder>() {

    private var context: Context? = null

    private var list: ArrayList<ForYouResponse>? = null
    private var width: Int = 0

    init {
        this.context = context
        this.list = list

    }

    interface ManageClick {
        fun managePlayVideoClick(position: Int?)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.other_user_new_adapter, parent, false)
        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        width = dpToPx(context?.resources?.getDimensionPixelSize(R.dimen._150sdp)!!)
        // holder.mVideoView.setImageBitmap(null)
        holder.mVideoView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.thumb))

        holder.mVideoView.setOnClickListener {
            holder.mVideoView.isEnabled = false
            Handler().postDelayed({ // This method will be executed once the timer is over
                holder.mVideoView.isEnabled = true
            }, 2000)

            manageClick.managePlayVideoClick(position)
        }
        holder.bindItems(position, list, context, width)
    }

    private fun dpToPx(dp: Int): Int {
        val density: Int = context?.resources?.displayMetrics?.density!!.toInt()
        return (dp * density)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mVideoView = itemView.findViewById(R.id.mVideoView) as AppCompatImageView

        fun bindItems(
            pos: Int, list: ArrayList<ForYouResponse>?, context: Context? = null, width: Int
        ) {

            val txt_title = itemView.findViewById(R.id.txt_title) as AppCompatTextView
            val tvpostCount = itemView.findViewById(R.id.tvpostCount) as AppCompatTextView
            val mPlayVideo = itemView.findViewById(R.id.mPlayVideo) as AppCompatImageView
            val iv_delete_post = itemView.findViewById(R.id.iv_delete_post) as ImageView
            val txt_videoDuration =
                itemView.findViewById(R.id.txt_videoDuration) as AppCompatTextView
            val txt_desc = itemView.findViewById(R.id.txt_desc) as AppCompatTextView
            //val url = URL(list!!.get(pos).getpost_video_thumbnail())
            iv_delete_post.visibility = View.GONE

            if (list!![pos].post_video_thumbnail_compres!!.contains("GIF") || list[pos].post_video_thumbnail_compres!!.contains(
                    "gif"
                )
            ) {
                mPlayVideo.visibility = View.GONE
                if (list[pos].post_height != null && list[pos].post_height != null) {

                    Glide.with(context!!)
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

                                if (list[pos].post_height != null && list[pos].post_width != null) {
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

                Glide.with(context!!)
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
                            Log.i(
                                "PlayerFragment",
                                "onNewResultImpl ${resource.width} * ${resource.height}"
                            )
                            if (resource.height > resource.width) {
                                Log.i("PlayerFragment", "Width $width")
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



            if (list != null && list.size > 0) {
                /*  mVideoView.setOnClickListener {
                      mVideoView.isEnabled = false
                      Handler().postDelayed({ // This method will be executed once the timer is over
                          mVideoView.isEnabled = true
                      }, 2000)

                      val intent = Intent(context, VideoDetailActivity::class.java)
                      intent.putExtra("position", pos)
                      intent.putExtra("cat_ID", "")
                      intent.putExtra("screen","otherProfile")
                      intent.putExtra("hashtag","")
                      context.startActivity(intent)
                  }*/
                /* mVideoView.setOnClickListener {
                     mVideoView.isEnabled = false
                     Handler().postDelayed({ // This method will be executed once the timer is over
                         mVideoView.isEnabled = true
                     }, 2000)

                     val intent = Intent(context, VideoPlayActivity::class.java)
                     intent.putExtra("response", list)
                     intent.putExtra("position", pos)
                     intent.putExtra("cat_ID", "")
                     intent.putExtra("isProfile", false)
                     intent.putExtra("is_discover", false)
                     intent.putExtra("likeCountMap", likeVideoHash)
                     intent.putExtra("hasLikedMap", has_likedMap)
                     intent.putExtra("followerMap", followerMap)
                     context.startActivity(intent)
                 }*/
/*
                mPlayVideo.setOnClickListener {
                    val intent = Intent(context,VideoPlayActivity::class.java)
                    intent.putExtra("response",list)
                    intent.putExtra("position",pos)
                    intent.putExtra("cat_ID","")
                    intent.putExtra("isProfile",false)
                    intent.putExtra("is_discover",false)
                    intent.putExtra("likeCountMap",likeVideoHash)
                    intent.putExtra("hasLikedMap",has_likedMap)
                    intent.putExtra("followerMap", followerMap)
                    context.startActivity(intent)
                }
*/

                var video_duration: String = list[pos].post_video_duration.toString()
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
                    txt_videoDuration.text = video_duration
                }

/*
                if (list[pos].post_view_count!=null) {
                    val viewCountsFinal: String
                    var  viewCounts: Int = list[pos].post_view_count!!.toInt()
                    if (viewCounts > 1000) {
                        viewCounts /= 1000
                        viewCountsFinal = viewCounts.toString() +"k"
                    } else {
                        viewCountsFinal = list[pos].post_view_count!!
                    }
                    tvpostCount.text = viewCountsFinal
                }
*/
                if (list[pos].post_view_count != null && list[pos].post_view_count != "") {
                    // showViewcounts(list[pos].post_view_count!!.toString(), tvpostCount)
                    Methods.showViewCounts(list[pos].post_view_count!!.toString(), tvpostCount)

                }

                txt_desc.text = list[pos].post_title
            }
        }


    }
}