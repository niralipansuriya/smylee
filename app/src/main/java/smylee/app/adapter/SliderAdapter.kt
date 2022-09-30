package smylee.app.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import smylee.app.R
import smylee.app.model.BannerResponse
import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
import java.util.*

class SliderAdapter(context: Context?, images: ArrayList<BannerResponse>?) : PagerAdapter() {
    private var images: ArrayList<BannerResponse>? = null
    private var inflater: LayoutInflater? = null
    private var context: Context? = null

    init {
        this.context = context
        this.images = images
        inflater = LayoutInflater.from(context)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun getCount(): Int {
        return images!!.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val myImageLayout: View = inflater!!.inflate(R.layout.slider_adapter, view, false)
        val myImage = myImageLayout.findViewById<View>(R.id.image) as ImageView
        val tvTag = myImageLayout.findViewById<View>(R.id.tvtag) as TextView
        tvTag.text = images!![position].tag_name

        Glide.with(context!!).load(images!![position].banner_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                override fun onResourceReady(
                    resource: Drawable?, model: Any?, target: Target<Drawable?>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    myImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    myImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    return false
                }
            }).into(myImage)

        myImage.setOnClickListener {
            val intent = Intent(context, HashTagDetailOrTrendingDetail::class.java)
            intent.putExtra("hashTag", images!![position].tag_name)
            context!!.startActivity(intent)
        }

        view.addView(myImageLayout, 0)
        return myImageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}