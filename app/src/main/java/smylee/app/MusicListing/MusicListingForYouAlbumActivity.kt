package smylee.app.MusicListing

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_music_listing_for_you_album.*
import smylee.app.R
import smylee.app.adapter.PagerAlmubMusicListingAdapter
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Logger
import smylee.app.utils.Utils

class MusicListingForYouAlbumActivity : BaseActivity() {

    var currentTab: Int = 0
    var strSearch: String = ""
    private var pageCount = 1
    var adapter: PagerAlmubMusicListingAdapter? = null
    private val ADD_AUDIO = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_listing_for_you_album)

        //supportActionBar!!.hide()

        back.setOnClickListener {
            stopAudio()
            onBackPressed()
        }

        tabalbum.addTab(tabalbum.newTab().setText("  For You"))
        tabalbum.addTab(tabalbum.newTab().setText("Album  "))

        tabalbum.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                adapter!!.onTabChanged(tab.position)
                currentTab = tab.position
                Logger.print("onTabSelected============$currentTab")
            }
        })

        val root: View = tabalbum.getChildAt(0)
        if (root is LinearLayout) {
            root.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            val drawable = GradientDrawable()
            drawable.setColor(ContextCompat.getColor(this, R.color.black))
            drawable.setSize(1, 0)
            root.dividerPadding = 40
            root.dividerDrawable = drawable
        }

        adapter = PagerAlmubMusicListingAdapter(
            supportFragmentManager,
            tabalbum.tabCount,
            resources.getString(R.string.for_you),
            resources.getString(R.string.album)
        )
        pager_music.adapter = adapter
        tabalbum.setupWithViewPager(pager_music)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("download_done"))

        iv_Search.setOnClickListener {
            Logger.print("iv_Search onclick============$currentTab")

            if (searchAudio.visibility == View.INVISIBLE) {
                searchAudio.visibility = View.VISIBLE
            } else if (searchAudio.query.isEmpty()) {
                searchAudio.visibility = View.INVISIBLE
            }
        }

        Logger.print("Selected album Id========" + AlbumFragment.AlBumID.toString())
        Logger.print("Selected album tab========" + AlbumFragment.currentAlbumTab)

        searchAudio.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                strSearch = query.trim() + ""

                pageCount = 1
                adapter!!.onSearch(strSearch, currentTab)
                searchAudio.isFocusable = false
                searchAudio.clearFocus()
                Utils.hideKeyboard(this@MusicListingForYouAlbumActivity)
                return true
            }


            override fun onQueryTextChange(s: String?): Boolean {
                if (searchAudio.query.isEmpty()) {

                    strSearch = ""
                    pageCount = 1
                    adapter!!.onSearch(strSearch, currentTab)
                    searchAudio.isFocusable = false
                    searchAudio.clearFocus()
                    Utils.hideKeyboard(this@MusicListingForYouAlbumActivity)
                }
                return false
            }

        })
    }

    override fun onBackPressed() {
        stopAudio()
        super.onBackPressed()
    }

    companion object {
        var final_Audio_File: String = ""
        var AUDIO_ID: String = ""
        var is_Download: String = ""
        var audioDuration: Long = 0L
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.print("onActivityResult onActivityResult onActivityResult !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        if (requestCode == ADD_AUDIO) {
            Logger.print("requestCode==ADD_AUDIO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

            if (resultCode == Activity.RESULT_OK) {
                Logger.print("resultCode == RESULT_OK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                if (data != null) {


                    Logger.print("data!=null !!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    val path = data.extras!!.getString("INTENT_AUDIO_FILE")
                    //val audioDuration = data.extras!!.getString("audioDuration")
                    //val audioId = data.extras!!.getInt("audioId")


                    if (path != null && path != "") {
                        final_Audio_File = fileName(path)
                        Logger.print("path  musicListingActivity!!!!!!!!!!" + path)

                    }
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(path)
                    mediaPlayer.prepare()


                    val duration = mediaPlayer.duration

                    val audioID = ForYouFragment.AUDIOID

                    if (duration != null && duration != 0) {
                        audioDuration = duration.toLong()
                    }

                    if (audioID != null && audioID != "") {
                        AUDIO_ID = audioID
                    }
                    is_Download = "1"

                    this.finish()

                    Logger.print("audioDuration  musicListingActivity!!!!!!!!!!" + duration)
                    Logger.print("audioId  musicListingActivity!!!!!!!!!!" + audioID)


                }


            }


        }


    }

    fun fileName(url: String?): String {
        val parts = url!!.split("/".toRegex()).toTypedArray()
        var result = parts[parts.size - 1]
        result = result.replace("+", "", true)
        result = result.replace("(", "", true)
        result = result.replace(")", "", true)
        result = result.replace("_", "", true)

        return result
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent_broadcast: Intent?) {
            if (intent_broadcast != null) {
                val audioFileName = intent_broadcast.getStringExtra("audio_file_name")
                AUDIO_ID = intent_broadcast.getStringExtra("audio_id")!!
                final_Audio_File = intent_broadcast.getStringExtra("audio_file_name")!!
                is_Download = "1"
                audioDuration = intent_broadcast.getIntExtra("audioDuration", 0).toLong()
                val isBack = intent_broadcast.getBooleanExtra("is_back", false)
                Logger.print("is_back==============$isBack")
                Logger.print("audio_file_name IN Broadcast receiver======$audioFileName")
                Logger.print("AUDIO ID IN Broadcast receiver======$AUDIO_ID")
                //onBackPressed()
                this@MusicListingForYouAlbumActivity.finish()
            }
        }
    }
}