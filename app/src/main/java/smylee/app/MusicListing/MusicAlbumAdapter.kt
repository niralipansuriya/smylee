package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView

import smylee.app.listener.OnPrepareListener
import smylee.app.R
import smylee.app.model.AlbumForYouResponse
import smylee.app.utils.Logger


class MusicAlbumAdapter(
    context: Context,
    album_list: ArrayList<AlbumForYouResponse>?,
    var manageClick: ManageClick
) :
    RecyclerView.Adapter<MusicAlbumAdapter.ViewHolder>() {

    interface ManageClick {
        fun manageClick(ID: String?, audio_uplink: String?)
    }

    private var context: Context? = null
    private var listener: AlbumTypeFragment.OnListFragmentInteractionListener? = null
    private var album_list: ArrayList<AlbumForYouResponse>? = null
    var lastPlayingPosition = -1

    init {
        this.context = context
        this.album_list = album_list
        this.listener = listener
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_for_you, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(position, this, album_list, manageClick)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return album_list!!.size
    }

    fun onTabChange() {
        for (i in 0 until album_list!!.size) {
            album_list!![i].isPlaying = 0
        }
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(pos: Int, forYouAdapter: MusicAlbumAdapter
            , album_list: ArrayList<AlbumForYouResponse>?, manageClick: ManageClick) {

            val tv_title = itemView.findViewById(R.id.tv_title) as AppCompatTextView
            val ll_parent = itemView.findViewById(R.id.ll_parent) as LinearLayout
            val ivPlayAudio = itemView.findViewById(R.id.ivPlayAudio) as RoundedImageView
            val progressAudio = itemView.findViewById(R.id.progressAudio) as ProgressBar

            Logger.print("bindItems !!!!!!!!!!!!!!!!!" + album_list?.size)
            ll_parent.setOnClickListener {
                album_list!![pos].isPlaying = 0
                ivPlayAudio.setImageResource(R.drawable.play_audio)
                progressAudio.visibility = View.GONE
                BaseActivity.stopAudio()
                manageClick.manageClick(album_list[pos].audio_id, album_list[pos].audio_uplink)
            }

            if (album_list!![pos].isPlaying == 1) {
                progressAudio.visibility = View.GONE
                ivPlayAudio.setImageResource(R.drawable.pause_audio)
            } else if (album_list[pos].isPlaying == 2) {
                progressAudio.visibility = View.VISIBLE
            } else {
                progressAudio.visibility = View.GONE
                ivPlayAudio.setImageResource(R.drawable.play_audio)
            }

            ivPlayAudio.setOnClickListener {
                if (album_list[pos].isPlaying == 1) {
                    BaseActivity.cancelPreparingAudio()
                    album_list[pos].isPlaying = 0
                    ivPlayAudio.setImageResource(R.drawable.play_audio)
                    progressAudio.visibility = View.GONE
                    BaseActivity.stopAudio()
                } else {
                    if (forYouAdapter.lastPlayingPosition != -1) {
                        album_list[forYouAdapter.lastPlayingPosition].isPlaying = 0
                    }
                    BaseActivity.cancelPreparingAudio()
                    forYouAdapter.lastPlayingPosition = pos
                    album_list[pos].isPlaying = 2
                    Log.i("MusicAlbumAdapter", "${album_list[pos].audio_uplink}")
                    BaseActivity.playAudio(
                        album_list[pos].audio_uplink,
                        object : OnPrepareListener {
                            override fun onPrepared() {
                                if (album_list.size > 0) {
                                    album_list[pos].isPlaying = 1
                                    forYouAdapter.notifyDataSetChanged()
                                }

                                /*progressAudio.visibility = View.GONE
                                ivPlayAudio.setImageResource(R.drawable.pause_audio)*/
                            }

                            override fun onCompleted() {
                                album_list[pos].isPlaying = 0
                                forYouAdapter.notifyDataSetChanged()
//                            ivPlayAudio.setImageResource(R.drawable.play_audio)
                            }
                        })
                    progressAudio.visibility = View.VISIBLE
                }
                forYouAdapter.notifyDataSetChanged()
            }

            tv_title.text = album_list[pos].audio_title
            val tv_desc = itemView.findViewById(R.id.tv_desc) as AppCompatTextView
            tv_desc.text = album_list[pos].audio_description
        }
    }
}