package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import android.content.Context
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


class ForYouAdapter(
    context: Context,
    album_list: ArrayList<AlbumForYouResponse>?,
    val manageClick: ManageClick
) : RecyclerView.Adapter<ForYouAdapter.ViewHolder>() {
    interface ManageClick {
        fun manageClick(ID: String?, audio_uplink: String?)
        fun manageAudioTrim(ID: String?, audio_uplink: String?)
    }

    private var context: Context? = null
    private var albumList: ArrayList<AlbumForYouResponse>? = null
    var lastPlayingPosition = -1

    init {
        this.context = context
        this.albumList = album_list
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_for_you, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(position, this, albumList, manageClick)
    }

    override fun getItemCount(): Int {
        return albumList!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            pos: Int,
            forYouAdapter: ForYouAdapter,
            album_list: ArrayList<AlbumForYouResponse>?,
            manageClick: ManageClick
        ) {
            val llUseAudio = itemView.findViewById(R.id.llUseAudio) as LinearLayout
            val llParent = itemView.findViewById(R.id.ll_parent) as LinearLayout
            val ivPlayAudio = itemView.findViewById(R.id.ivPlayAudio) as RoundedImageView
            val progressAudio = itemView.findViewById(R.id.progressAudio) as ProgressBar

            llParent.setOnClickListener {
                manageClick.manageAudioTrim(
                    album_list!![pos].audio_id,
                    album_list[pos].audio_uplink
                )

            }

            llUseAudio.setOnClickListener {
                album_list!![pos].isPlaying = 0
                ivPlayAudio.setImageResource(R.drawable.play_audio)
                progressAudio.visibility = View.GONE
                BaseActivity.stopAudio()
                manageClick.manageClick(album_list[pos].audio_id, album_list[pos].audio_uplink)
            }

            /*if(album_list!![pos].isPlaying) {
                ivPlayAudio.setImageResource(R.drawable.pause_audio)
            } else {
                ivPlayAudio.setImageResource(R.drawable.play_audio)
            }*/

            when {
                album_list!![pos].isPlaying == 1 -> {
                    progressAudio.visibility = View.GONE
                    ivPlayAudio.setImageResource(R.drawable.pause_audio)
                }
                album_list[pos].isPlaying == 2 -> {
                    progressAudio.visibility = View.VISIBLE
                }
                else -> {
                    progressAudio.visibility = View.GONE
                    ivPlayAudio.setImageResource(R.drawable.play_audio)
                }
            }

            ivPlayAudio.setOnClickListener {
                smylee.app.utils.Logger.print("isPlaying !!!!!!!!!!!!!!!!!!!!!!!!!!!!" + album_list[pos].isPlaying)
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
                    BaseActivity.playAudio(
                        album_list[pos].audio_uplink,
                        object : OnPrepareListener {
                            override fun onPrepared() {
                                //BaseActivity.stopAudio()
                                album_list[pos].isPlaying = 1
                                forYouAdapter.notifyDataSetChanged()
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

            val tvTitle = itemView.findViewById(R.id.tv_title) as AppCompatTextView
            tvTitle.text = album_list[pos].audio_title.trim()
            val tvDesc = itemView.findViewById(R.id.tv_desc) as AppCompatTextView
            tvDesc.text = album_list[pos].audio_description.trim()
        }
    }
}