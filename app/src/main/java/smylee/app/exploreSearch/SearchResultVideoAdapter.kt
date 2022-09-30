package smylee.app.exploreSearch

import smylee.app.ui.base.BaseActivity
import android.content.Intent
import android.graphics.Bitmap
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
import com.makeramen.roundedimageview.RoundedImageView
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.model.ForYouResponse
import smylee.app.home.HomeActivity
import smylee.app.utils.Logger
import smylee.app.R
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.utils.Methods
import java.text.DecimalFormat

class SearchResultVideoAdapter(
    val context: BaseActivity,
    var USER_ID: String,
    var followerMap: HashMap<String, String>,
    var has_likedMap: HashMap<String, String>,
    var likeVideoHash: HashMap<String, String>,
    var searchResultList: ArrayList<ForYouResponse>?,
    val manageClick: ManageClick,
    private var width: Int = 0
) : RecyclerView.Adapter<SearchResultVideoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgVideoThumb = itemView.findViewById(R.id.imgVideoThumb) as AppCompatImageView
        val imgPlay = itemView.findViewById(R.id.imgPlay) as AppCompatImageView
        val imgProPic = itemView.findViewById(R.id.imgProPic) as RoundedImageView

        fun bindItems(
            position: Int,
            searchResultList: ArrayList<ForYouResponse>?,
            context: BaseActivity,
            USER_ID: String,
            width: Int
        ) {
            val tvViews = itemView.findViewById(R.id.tvViews) as AppCompatTextView
            val tvDuration = itemView.findViewById(R.id.tvDuration) as AppCompatTextView
            val tvUserName = itemView.findViewById(R.id.tvUserName) as AppCompatTextView
            val tvTitle = itemView.findViewById(R.id.tvTitle) as AppCompatTextView

            if (searchResultList != null && searchResultList.size > 0) {
                var videoDuration: String =
                    searchResultList[position].post_video_duration.toString()
                if (videoDuration.contains(".")) {
                    val separated: List<String> = videoDuration.split(".")
                    Log.d("separated one=========", separated[0])
                    Log.d("separated two=========", separated[1])

                    if (separated[1].length > 1) {
                        Logger.print("separated size greter !!!!!!!!" + separated.size)
                        videoDuration = separated[0] + ":" + separated[1]
                    } else {
                        Logger.print("separated size not grater!!!!!!!!" + separated.size)
                        videoDuration = separated[0] + ":" + separated[1] + "0"
                    }
                } else {
                    videoDuration = "$videoDuration:00"
                }
                tvDuration.text = videoDuration


                if (searchResultList[position].post_view_count != null && searchResultList[position].post_view_count != "") {
                    Methods.showViewCounts(
                        searchResultList[position].post_view_count!!.toString(),
                        tvViews
                    )

                }

                tvUserName.text = searchResultList[position].profile_name
                tvTitle.text = searchResultList[position].post_title

                if (searchResultList[position].profile_pic != null && searchResultList[position].profile_pic != "") {
                    imgProPic.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(context)
                        .load(searchResultList[position].profile_pic)
                        .error(R.drawable.userpicfinal)
                        .into(imgProPic)
                } else {
                    imgProPic.scaleType = ImageView.ScaleType.FIT_CENTER
                    imgProPic.setImageResource(R.drawable.userpicfinal)
                }

                if (searchResultList!![position].post_video_thumbnail_compres!!.contains("GIF") || searchResultList[position].post_video_thumbnail_compres!!.contains(
                        "gif"
                    )
                ) {
                    imgPlay.visibility = View.GONE

                    Glide.with(context)
                        .asGif()
                        .error(R.drawable.thumb)
                        .placeholder(R.drawable.thumb)
                        .load(searchResultList[position].post_video_thumbnail_compres)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<GifDrawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<GifDrawable?>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                imgVideoThumb.scaleType = ImageView.ScaleType.CENTER_CROP
                                return false
                            }

                            override fun onResourceReady(
                                resource: GifDrawable?,
                                model: Any?,
                                target: Target<GifDrawable?>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (searchResultList.size > 0) {
                                    if (searchResultList!![position].post_height!! > searchResultList!![position].post_width!!) {
                                        imgVideoThumb.scaleType = ImageView.ScaleType.CENTER_CROP

                                    } else {
                                        imgVideoThumb.scaleType = ImageView.ScaleType.FIT_CENTER

                                    }
                                }

                                return false

                            }


                        })

                        .into(imgVideoThumb)

                } else {
                    imgPlay.visibility = View.VISIBLE

                    Glide.with(context)
                        .asBitmap()
                        .error(R.drawable.thumb)
                        .placeholder(R.drawable.thumb)
                        .load(searchResultList!![position].post_video_thumbnail_compres)
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
                                    imgVideoThumb.scaleType = ImageView.ScaleType.CENTER_CROP
                                    imgVideoThumb.setImageBitmap(resource)
                                } else {
                                    val layoutParams = imgVideoThumb.layoutParams
                                    layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                                    imgVideoThumb.scaleType = ImageView.ScaleType.FIT_CENTER
                                    imgVideoThumb.setImageBitmap(resource)
                                }
                            }
                        })
                }


                if (!USER_ID.contentEquals(searchResultList[position].user_id.toString())) {
                    imgProPic.setOnClickListener {
                        if (context is HomeActivity) {
                            val homeActivity: HomeActivity = context
                            homeActivity.isAttachFragmentAgain = false
                        }
                        val intent = Intent(context, OtherUserProfileActivity::class.java)
                        intent.putExtra(
                            "OTHER_USER_ID",
                            searchResultList[position].user_id.toString()
                        )
                        context.startActivity(intent)
                    }
                }
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.search_result_video_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return searchResultList!!.size
    }

    private fun dpToPx(dp: Int): Int {
        val density: Int = context.resources?.displayMetrics?.density!!.toInt()
        return (dp * density)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface ManageClick {
        fun managePlayVideoClick(position: Int?)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        width = dpToPx(context.resources?.getDimensionPixelSize(R.dimen._150sdp)!!)

        holder.imgProPic.setImageBitmap(null)
        holder.imgVideoThumb.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.thumb))

        //     holder.imgVideoThumb.setImageBitmap(null)
        holder.bindItems(position, searchResultList, context, USER_ID, width)


        holder.imgVideoThumb.setOnClickListener {
            if (context is HomeActivity) {
                val homeActivity: HomeActivity = context
                homeActivity.isAttachFragmentAgain = false
            }
            manageClick.managePlayVideoClick(position)

        }

    }

}