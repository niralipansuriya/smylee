package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_album_type.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.AlbumForYouResponse
import smylee.app.model.AlmunListResponse
import smylee.app.utils.*
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

//class AlbumTypeFragment(val  manageClick: ManageClick) : Fragment()
class AlbumTypeFragment : Fragment() {
    var audio_file_name: String = ""
    private var audio_file_ID: String = ""
    private var searchText: String = ""
    private var page_count = 1
    private var album_list = ArrayList<AlbumForYouResponse>()
    private val ARG_COLUMN_COUNT = "column-count"
    private var mColumnCount = 1

    private var llm: LinearLayoutManager? = null
    private var currentVisiblecount: Int = 0

    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private lateinit var musicListingViewModel: MusicListingViewModel
    lateinit var activity: BaseActivity
    var cmpagination: CustomPaginationDialog? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var musicAlbumAdapter: MusicAlbumAdapter? = null
    private var audioDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = arguments
        if (b != null) {
            mColumnCount = b.getInt(ARG_COLUMN_COUNT)
            album_list = b.getSerializable("album_list") as ArrayList<AlbumForYouResponse>
            audio_file_ID = b.getString("album_ID", "")
            searchText = b.getString("searchText", "")

//            if (searchText!="")
//            {
//                AudioDetailListing(audio_file_ID)
//            }
            if (album_list != null) {
                Log.i("AlbumTypeFragment", "Album List ${album_list.size}")
            } else {
                Log.i("AlbumTypeFragment", "Album List is null")
            }
        } else {
            Log.i("AlbumTypeFragment", "Bundle is null")
        }
    }

    private fun removeLoaderView() {
        if (cmpagination != null && cmpagination!!.isShowing) {
            cmpagination!!.hide()
        }

    }

    private fun addViewLoadingView() {
        if (cmpagination == null)
            cmpagination = CustomPaginationDialog(getActivity())
        cmpagination!!.show(false)

    }

    private fun ApplyPagination(audio_ID: String) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentVisiblecount = llm?.childCount!!
                totalItemCount = llm?.itemCount!!
                lastVisibleItem = llm?.findLastVisibleItemPosition()!!
                if (!isLoading) {
                    if (!isDataFinished) {
                        if (lastVisibleItem >= (totalItemCount - 1))
                        //if (lastVisibleItem ==0)
                        {
                            isLoading = true
                            page_count++
                            if (dy > 0) {
                                addViewLoadingView()

                            }
                            Handler().postDelayed({ AudioDetailListing(audio_ID) }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun AudioDetailListing(audio_ID: String) {
        Logger.print("audio_ID In AudioDetailListing=============$audio_ID")
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = page_count.toString()
        hashMap["album_id"] = audio_ID
        // hashMap["search_text"] = ""
        hashMap["search_text"] = searchText

//        val apiName: String = APIConstants.ALBUMAUDIODETAIL
        val isProgressShow = page_count == 1
        musicListingViewModel.observeAlbumAudioListingDetail(
            activity,
            hashMap,
            isProgressShow
        ).observe(this, Observer {
            if (it != null) {
               // Utils.hideKeyboard(activity)

                removeLoaderView()
                Logger.print("Audio by album ID response =====$it")

                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                val pagination_count: Int
                if (jsonObject.has("pagination_count")) {
                    pagination_count = jsonObject.getInt("pagination_count")
                    Logger.print("pagination_count==========$pagination_count")
                }

                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONArray("data")
                        val array =
                            Gson().fromJson<ArrayList<AlbumForYouResponse>>(jsonobj1.toString(),
                                object : TypeToken<ArrayList<AlbumForYouResponse>>() {}.type
                            )

                        if (page_count == 1) {
                            album_list.clear()
                        }

                        if (array.isNotEmpty()) {
                            isDataFinished = false
                            album_list.addAll(array)
                        } else {
                            isDataFinished = true
                        }


                        if (album_list.size > 0) {
                            Logger.print("album_list !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                            Logger.print("album_list sizeeeeeeeeeeeeeeeeeeee!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + album_list.size)
                            list.visibility = View.VISIBLE
                            isLoading = false
                        } else if (page_count == 1) {
                            list.visibility = View.GONE
                            tvnorecord.visibility = View.VISIBLE
                        }
                        setAdapter()
                    }
                } else if (code == 0) {
                    if (page_count == 1) {
                        tvnorecord.visibility = View.VISIBLE
                        setAdapter()
                    }
                } else {
                    if (page_count == 1) {
                        Utils.showToastMessage(
                            context = activity,
                            message = jsonObject["message"].toString()
                        )
                        setAdapter()
                    }
                }
            }
        })
    }


    fun OnSearch(searchtext: String) {
        searchText = searchtext
        if (::activity.isInitialized) {
            Logger.print("OnSearch!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            musicAlbumAdapter = null
            album_list.clear()
            page_count = 1
            if (!audio_file_ID.contentEquals("")) {
                Logger.print("audio_file_ID not blank!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                AudioDetailListing(audio_file_ID)
            } else {
                Log.i("AlbumTypeFragment", "audio file id is empty")
            }
        }
    }

    fun onTabChanged(audioFileId: String) {
        /*if(musicAlbumAdapter != null) {
            musicAlbumAdapter?.notifyDataSetChanged()
        }*/
        if (::activity.isInitialized) {
            musicAlbumAdapter = null
            album_list.clear()
            page_count = 1
            if (!audioFileId.contentEquals("")) {
                AudioDetailListing(audio_file_ID)
            } else {
                Log.i("AlbumTypeFragment", "audio file id is empty")
            }
        } else {
            Handler().postDelayed({
                if (::activity.isInitialized) {
                    musicAlbumAdapter = null
                    album_list.clear()
                    page_count = 1
                    if (!audioFileId.contentEquals("")) {
                        AudioDetailListing(audio_file_ID)
                    }
                }
            }, 300)
        }
    }

    fun onTabChanged(audioList: ArrayList<AlbumForYouResponse>, audioFileId: String) {
        /*if(musicAlbumAdapter != null) {
            musicAlbumAdapter?.notifyDataSetChanged()
        }*/
        if (::activity.isInitialized) {
            musicAlbumAdapter = null
            album_list.clear()
            if (audioList != null && audioList.size > 0) {
                Logger.print("audioList != null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                album_list.addAll(audioList)
                setAdapter()
            } else {
                Logger.print("else audioList != null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                page_count = 1
                if (!audioFileId.contentEquals("")) {
                    AudioDetailListing(audio_file_ID)
                }
            }
        } else {
            Handler().postDelayed({
                if (::activity.isInitialized) {
                    musicAlbumAdapter = null
                    album_list.clear()
                    if (audioList != null && audioList.size > 0) {
                        Logger.print("postDelayed audioList != null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                        album_list.addAll(audioList)
                        setAdapter()
                    } else {
                        page_count = 1
                        if (!audioFileId.contentEquals("")) {
                            Logger.print("else postDelayed audioList != null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                            AudioDetailListing(audio_file_ID)
                        }
                    }
                }
            }, 300)
        }
    }

    interface ManageClick {
        fun manageApiCall()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mColumnCount <= 1) {
            list.layoutManager = LinearLayoutManager(context)
        } else {
            list.layoutManager = GridLayoutManager(context, mColumnCount)
        }
        musicListingViewModel = ViewModelProviders.of(this).get(MusicListingViewModel::class.java)
        musicAlbumAdapter = null
        if (album_list != null && album_list.size > 0) {
            setAdapter()
        } else {
            Log.i("AlbumTypeFragment", "Album List is empty")
        }
        ApplyPagination(audio_file_ID)
    }

    override fun onPause() {
        super.onPause()
        BaseActivity.stopAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseActivity.stopAudio()
    }

    override fun onDetach() {
        super.onDetach()
        BaseActivity.stopAudio()
    }

    private fun setAdapter() {
        if (musicAlbumAdapter == null) {
            Logger.print("musicAlbumAdapter null ==============================$album_list")
            if (list != null) {
                llm = LinearLayoutManager(context)
                list.layoutManager = llm
                musicAlbumAdapter =
                    MusicAlbumAdapter(activity, album_list, object : MusicAlbumAdapter.ManageClick {
                        override fun manageClick(ID: String?, audio_uplink: String?) {
                            if (ID != null && !ID.contentEquals("") && audio_uplink != null && !audio_uplink.contentEquals(
                                    ""
                                )
                            ) {
                                val extention: String = audio_uplink.substring(
                                    audio_uplink.lastIndexOf("."),
                                    audio_uplink.length
                                )
                                audio_file_name = fileName(audio_uplink)
                                Logger.print("audio_file_name============$audio_file_name")
                                DownloadFileFromURL(
                                    extention,
                                    activity,
                                    audio_file_name,
                                    ID
                                ).execute(audio_uplink)
                            } else {
                                Utils.showToastMessage(activity, "Audio Not Found")
                            }
                        }
                    })
                list.adapter = musicAlbumAdapter
            } else {
                Handler().postDelayed({
                    if (list != null) {
                        llm = LinearLayoutManager(context)
                        list.layoutManager = llm
                        musicAlbumAdapter = MusicAlbumAdapter(activity, album_list,
                            object : MusicAlbumAdapter.ManageClick {
                                override fun manageClick(ID: String?, audio_uplink: String?) {
                                    if (ID != null && !ID.contentEquals("") && audio_uplink != null && !audio_uplink.contentEquals(
                                            ""
                                        )
                                    ) {
                                        val extention: String = audio_uplink.substring(
                                            audio_uplink.lastIndexOf("."),
                                            audio_uplink.length
                                        )
                                        audio_file_name = fileName(audio_uplink)
                                        Logger.print("audio_file_name============$audio_file_name")
                                        DownloadFileFromURL(
                                            extention,
                                            activity,
                                            audio_file_name,
                                            ID
                                        ).execute(audio_uplink)
                                    } else {
                                        Utils.showToastMessage(activity, "Audio Not Found")
                                    }
                                }
                            })
                        list.adapter = musicAlbumAdapter
                    } else {
                        Log.i("AlbumTypeFragment", "List is null")
                    }
                }, 200)
            }
        } else {
            Logger.print("musicAlbumAdapter not null notifyDataSetChanged==============================$album_list")
            musicAlbumAdapter?.notifyDataSetChanged()
        }
    }

    fun fileName(url: String?): String {
        val parts = url!!.split("/".toRegex()).toTypedArray()
        var result = parts[parts.size - 1]
        result = result.replace("+", "", true)
        result = result.replace("(", "", true)
        result = result.replace(")", "", true)
        result = result.replace("-", "", true)
        audio_file_name = result
        println("name in  FileName>>>>$audio_file_name")
        return audio_file_name
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    private class DownloadFileFromURL internal constructor(
        Extenson: String, var activity: BaseActivity,
        var audio_file_name: String, var audio_ID: String
    ) : AsyncTask<String?, String?, String?>() {
        var cm: CustomLoaderDialog? = null
        var audioDuration: Int = 0
        var nameoffile: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            if (cm == null)
                cm = CustomLoaderDialog(activity)
            cm!!.show(false)
        }

        private fun back() {
            val intent = Intent("download_done")
            intent.putExtra("audio_file_name", audio_file_name)
            intent.putExtra("audio_id", audio_ID)
            intent.putExtra("is_back", true)
            intent.putExtra("audioDuration", audioDuration)
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)

//            activity.onBackPressed()
//            activity.finish()
        }

        override fun onProgressUpdate(vararg values: String?) {
        }

        override fun onPostExecute(file_url: String?) {
            try {
                if (cm != null && cm!!.isShowing) {
                    cm!!.hide()
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cm = null
            }

            if (file_url == "Success") {
                back()
            } else {
                Utils.showToastMessage(
                    context = activity,
                    message = activity.resources.getString(R.string.error_downloading_audio)
                )
            }
        }

        init {
            println("EXTENSION OF FILE::::::::::$Extenson")
        }

        override fun doInBackground(vararg p0: String?): String? {
            var count: Int
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                val url = URL(p0[0])
                val urofysllabusl: String = p0.get(0)!!
                println("urofysllabusl::::::$urofysllabusl")
                val parts = urofysllabusl.split("/".toRegex()).toTypedArray()
                println("parts::::::::::::::$parts")
                var result = parts[parts.size - 1]
                result = result.replace("+", "", true)
                result = result.replace("(", "", true)
                result = result.replace(")", "", true)
                result = result.replace("-", "", true)
                println("result:::::::::::$result")
                nameoffile = result

                println("result::::::doInback::::$result")
                println("name in  doInBackground>>>>$nameoffile")
                val conection = url.openConnection()
                conection.connect()
                val lenghtOfFile = conection.contentLength

                input = BufferedInputStream(url.openStream())
                output = FileOutputStream(Constants.Audio_PATH + "" + nameoffile)

                println("output:::::::::$output")
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()

                val audioFilepath = Constants.Audio_PATH + audio_file_name
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(audioFilepath)
                mediaPlayer.prepare()
                audioDuration = mediaPlayer.duration
            } catch (e: Exception) {
                e.printStackTrace()
                if (output != null) {
                    output.flush()
                    output.close()
                }
                input?.close()
                return "error"
            }
            return "Success"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album_type, container, false)
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: ArrayList<AlmunListResponse>)
    }

    companion object {
        fun newInstance() = AlbumTypeFragment()

    }
}