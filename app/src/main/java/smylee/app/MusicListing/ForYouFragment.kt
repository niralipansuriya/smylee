package smylee.app.MusicListing

import smylee.app.ui.base.BaseActivity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_for_you.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.audiotrim.customAudioViews.AudioTrimmerActivity
import smylee.app.model.AlbumForYouResponse
import smylee.app.utils.*
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


class ForYouFragment : Fragment() {
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisiblecount: Int = 0
    private lateinit var musicListingViewModel: MusicListingViewModel
    lateinit var activity: BaseActivity
    private var page_count = 1
    private var album_list = ArrayList<AlbumForYouResponse>()
    var cmpagination: CustomPaginationDialog? = null
    private val ADD_AUDIO = 1001
    private var mWasGetContentIntent = false
    private val REQUEST_CODE_EDIT = 1
    var audio_file_name: String = ""
    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
    private var forYouAdapter: ForYouAdapter? = null

    var searchAudio = ""

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicListingViewModel = ViewModelProviders.of(this).get(MusicListingViewModel::class.java)


        //   mWasGetContentIntent = getIntent().

        mWasGetContentIntent = getActivity()!!.intent.action.equals(Intent.ACTION_GET_CONTENT)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }*/

    override fun onPause() {
        super.onPause()
        BaseActivity.stopAudio()
    }

    override fun onDetach() {
        super.onDetach()
        BaseActivity.stopAudio()

    }

    override fun onDestroy() {
        super.onDestroy()
        BaseActivity.stopAudio()

    }

    fun searchText(strSearch: String) {
        BaseActivity.cancelPreparingAudio()
        BaseActivity.stopAudio()
        searchAudio = strSearch
        page_count = 1
        album_list.clear()
        getAudioForYoulist(strSearch)
    }


    override fun onResume() {
        super.onResume()
        page_count = 1
        // getAudioForYoulist("")
        getAudioForYoulist(searchAudio)
        applyPagination()
    }


    fun onTabChanged() {
        for (i in 0 until album_list.size) {
            album_list[i].isPlaying = 0
        }
        if (forYouAdapter != null) {
            forYouAdapter!!.notifyDataSetChanged()
        }
        BaseActivity.stopAudio()
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

    private fun getAudioForYoulist(searchText: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = page_count.toString()
        hashMap["search_text"] = searchText
        val isProgressShow = page_count == 1
        musicListingViewModel.observeAudioListinfgForyou(activity, hashMap, isProgressShow)
            .observe(
                this, Observer {
                    removeLoaderView()
                 //   Utils.hideKeyboard(activity)

                    if (it != null) {
                        Logger.print("Foryou response =====$it")

                        val jsonObject = JSONObject(it.toString())
                        val code = jsonObject.getInt("code")



                        if (code == 1) {
                            /*if (page_count == 1) {

                            }*/
                            if (jsonObject.has("data")) {
                                val jsonobj1 = jsonObject.getJSONArray("data")
                                val array = Gson().fromJson<ArrayList<AlbumForYouResponse>>(
                                    jsonobj1.toString(),
                                    object : TypeToken<ArrayList<AlbumForYouResponse>>() {}.type
                                )
                                if (page_count == 1) {
                                    album_list.clear()
                                }

                                if (array.isNotEmpty()) {
                                    album_list.addAll(array)
                                } else {
                                    isDataFinished = true
                                }

                                if (album_list.size > 0) {
                                    rec_foryou.visibility = View.VISIBLE
                                    isLoading = false
                                    setAdapter()
                                } else {
                                    rec_foryou.visibility = View.GONE
                                }
                            }
                        } else if (code == 0) {
                            if (page_count == 1) {
                                Utils.showToastMessage(
                                    context = activity,
                                    message = jsonObject["message"].toString()
                                )
                            }
                        } else {
                            if (page_count == 1) {
                                Utils.showToastMessage(
                                    context = activity,
                                    message = jsonObject["message"].toString()
                                )
                            }
                        }
                    }
                }
            )
    }

    private fun setAdapter() {
        if (forYouAdapter == null) {
            llm = LinearLayoutManager(context)
            rec_foryou.layoutManager = llm
            forYouAdapter = ForYouAdapter(activity, album_list, object : ForYouAdapter.ManageClick {
                override fun manageClick(ID: String?, audio_link: String?) {
                    if (audio_link != null && ID != null && !ID.contentEquals("") && !audio_link.contentEquals(
                            ""
                        )
                    ) {
                        val extention: String =
                            audio_link.substring(audio_link.lastIndexOf("."), audio_link.length)
                        audio_file_name = fileName(audio_link)
                        Logger.print("audio_file_name============$audio_file_name")
                        DownloadFileFromURL(
                            extention,
                            activity,
                            audio_file_name,
                            ID,
                            false,
                            ADD_AUDIO,
                            getActivity(),
                            mWasGetContentIntent,
                            REQUEST_CODE_EDIT
                        ).execute(audio_link)
                    } else {
                        Utils.showToastMessage(activity, "Audio Not Found")
                    }
                }

                override fun manageAudioTrim(ID: String?, audio_uplink: String?) {
                    Logger.print("audio_uplink************************" + audio_uplink)
                    Logger.print("ID************************" + ID)

                    /*  if (audio_uplink != null && ID!=null && !ID.contentEquals("")&&!audio_uplink.contentEquals(""))
                      {
                          val extention: String = audio_uplink.substring(audio_uplink.lastIndexOf("."), audio_uplink.length)
                          audio_file_name = fileName(audio_uplink)
                          Logger.print("audio_file_name============$audio_file_name")
                          DownloadFileFromURL(extention, activity, audio_file_name,ID,true,ADD_AUDIO,getActivity(),mWasGetContentIntent,REQUEST_CODE_EDIT).execute(audio_uplink)
                      }*/

                }
            })
            rec_foryou.adapter = forYouAdapter
        } else {
            forYouAdapter?.notifyDataSetChanged()
        }
    }

    fun fileName(url: String?): String {
        val parts = url!!.split("/".toRegex()).toTypedArray()
        var result = parts[parts.size - 1]
        result = result.replace("+", "", true)
        result = result.replace("(", "", true)
        result = result.replace(")", "", true)
        result = result.replace("_", "", true)
        audio_file_name = result

        println("name in  FileName>>>>$audio_file_name")
        return audio_file_name
    }


    private class DownloadFileFromURL internal constructor(
        Extenson: String,
        var activity: BaseActivity,
        var audio_file_name: String,
        var AUDIO_ID: String,
        var isTrim: Boolean,
        val ADD_AUDIO: Int,
        var frag: FragmentActivity?,
        var mWasGetContentIntent: Boolean,
        var REQUEST_CODE_EDIT: Int
    ) : AsyncTask<String?, String?, String?>() {
        var cm: CustomLoaderDialog? = null
        var audioDuration: Int = 0
        var nameoffile: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            if (cm == null)
                cm = CustomLoaderDialog(activity)
            cm!!.show(false)
            // DialogUtils.showProgressDialog(activity, "")
        }


        private fun back() {

            val intent = Intent("download_done")
            intent.putExtra("audio_file_name", audio_file_name)
            intent.putExtra("audio_id", AUDIO_ID)
            intent.putExtra("is_back", false)
            intent.putExtra("audioDuration", audioDuration)
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
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
                if (!isTrim) {
                    back()
                } else {
                    AUDIOID = AUDIO_ID
                    var pathAudio: String = Constants.Audio_PATH
                    pathAudio += audio_file_name


                    var intent = Intent(frag, AudioTrimmerActivity::class.java)
                    intent.putExtra("AudioPath", pathAudio)
                    intent.putExtra("audioDuration", audioDuration)
                    intent.putExtra("audioId", AUDIO_ID)
                    frag?.startActivityForResult(intent, ADD_AUDIO)

                    //var intent = Intent(frag,RingdroidEditActivity::class.java)
/*
                   var intent = Intent(frag, RingdroidEditActivity::class.java)
                    intent.putExtra("mFilename",pathAudio)

                    intent.putExtra("was_get_content_intent", mWasGetContentIntent)
                    frag?.startActivityForResult(intent,REQUEST_CODE_EDIT)
*/

                }
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
                val urofysllabusl: String = p0[0]!!
                println("urofysllabusl::::::$urofysllabusl")
                val parts = urofysllabusl.split("/".toRegex()).toTypedArray()
                println("parts::::::::::::::$parts")
                var result = parts[parts.size - 1]
                result = result.replace("+", "", true)
                result = result.replace("(", "", true)
                result = result.replace(")", "", true)
                result = result.replace("_", "", true)
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
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())

                    // writing data to file
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Logger.print("call onActivityResult ******************")
        if (requestCode == ADD_AUDIO) {
            Logger.print("requestCode==ADD_AUDIO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

            if (resultCode == RESULT_OK) {
                Logger.print("resultCode == RESULT_OK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

                if (data != null) {

                    Logger.print("data!=null !!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    val path = data.extras!!.getString("INTENT_AUDIO_FILE")

                    Logger.print("path !!!!!!!!!!" + path)


                }


            }


        }

    }

    private fun applyPagination() {
        rec_foryou.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentVisiblecount = llm?.childCount!!
                totalItemCount = llm?.itemCount!!
                lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
                if (!isLoading) {
                    if (!isDataFinished) {
                        if (totalItemCount - currentVisiblecount <= lastVisibleItem + visibleThreshold) {
                            isLoading = true
                            page_count++
                            if (dy > 0) {
                                addViewLoadingView()

                            }
                            Handler().postDelayed(
                                { getAudioForYoulist(searchAudio) },
                                Constants.DELAY
                            )
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_for_you, container, false)
    }

    companion object {
        var AUDIOID = ""
    }
}