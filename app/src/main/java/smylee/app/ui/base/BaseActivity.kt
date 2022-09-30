package smylee.app.ui.base

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.danikula.videocache.HttpProxyCacheServer
import com.urfeed.listener.OnResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.api.APIClient
import smylee.app.api.APIConst
import smylee.app.camerfilters.FilterType
import smylee.app.listener.OnPrepareListener
import smylee.app.model.FilterModel
import smylee.app.utils.Constants
import smylee.app.utils.CustomLoaderDialog
import smylee.app.utils.SharedPreferenceUtils
import java.util.*
import kotlin.collections.HashMap


open class BaseActivity : FragmentActivity() {

    var progressDialog: CustomLoaderDialog? = null
    private var isCallRefreshToken = false
    /*private var currentDownloadingPos = -1
    private var initNextVideoListener: InitNextVideoListener? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (progressDialog == null) {
            progressDialog = CustomLoaderDialog(this)
        }
    }

    fun showProgressDialog() {
        progressDialog?.show(false)
    }

    fun hideProgressDialog() {
        if (progressDialog?.isShowing!!) {
            progressDialog?.hide()
        }
    }

    fun callAPI(
        url: String, requestType: Int, progress: Boolean, isProgressCancelable: Boolean,
        params: HashMap<String, Any?>,
        onResponse: OnResponse
    ) {
        if (!hasInternet()) {
            Toast.makeText(this, R.string.no_internet_available, Toast.LENGTH_SHORT).show()
            if (progressDialog?.isShowing!!) {
                progressDialog?.hide()
            }
            return
        }

        if (progress) {
            if (progressDialog?.isShowing!!) {
                progressDialog?.hide()
            }
            progressDialog?.show(isProgressCancelable)
        }

        val headerMap = HashMap<String, Any>()
        headerMap["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        headerMap["auth_token"] = SharedPreferenceUtils.getInstance(this)
            .getStringValue(Constants.AUTH_TOKEN_PREF, "") + ""
        headerMap["Content-Type"] = "application/json"

        val call: Call<String>?
        call = when (requestType) {
            APIConst.POST -> {
                APIClient.getClient()?.postRequest(url, headerMap, JSONObject(params).toString())
            }
            APIConst.PUT -> {
                APIClient.getClient()?.putRequest(url, headerMap, JSONObject(params).toString())
            }
            else -> {
                APIClient.getClient()?.getRequest(url, headerMap, params)
            }
        }
        if (isCallRefreshToken) {
            progressDialog?.hide()
            return
        }
        call?.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog?.hide()
                onResponse.onFailure(call, t)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {
                    progressDialog?.hide()
                    onResponse.onSuccess(response.code(), response.body().toString())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun hasInternet(): Boolean {
        return true
    }

    /*private val cacheListener = CacheListener { _, url, percentsAvailable ->
        println("progress==$percentsAvailable -->$url")
        if (percentsAvailable > 50) {
            if (preDownloadAsync != null) {
                preDownloadAsync?.cancel(true)
            }
            if (initNextVideoListener != null && currentDownloadingPos != -1) {
                initNextVideoListener?.onInitNextCall(currentDownloadingPos)
                currentDownloadingPos = -1
            }
        }
    }*/

    /*private val cacheListener2 = CacheListener { _, url, percentsAvailable ->
        println("progress2==$percentsAvailable -->$url")
        if(percentsAvailable > 50) {
            if(preDownloadAsyncSec != null) {
                preDownloadAsyncSec?.cancel(true)
            }
            if(initNextVideoListener != null && currentDownloadingPos != -1) {
                initNextVideoListener?.onInitNextCall(currentDownloadingPos)
                currentDownloadingPos = -1
            }
        }
    }

    fun clearDownloadedCache() {
        if(httpProxyCacheServer == null) {
            getProxy(this)
        }
        httpProxyCacheServer?.isCached("")
    }*/

    /*fun preDownload(
        url: String?,
        urlSec: String?,
        currentPos: Int,
        initNextVideoListener: InitNextVideoListener?
    ) {
        if (url != null) {
            currentDownloadingPos = currentPos
            this.initNextVideoListener = initNextVideoListener
            if (preDownloadAsync != null) {
                preDownloadAsync?.cancel(false)
            }
            if (httpProxyCacheServer == null) {
                getProxy(this)
            }
            httpProxyCacheServer?.unregisterCacheListener(cacheListener)
            if (httpProxyCacheServer?.isCached(url)!!) {
                Logger.print("BaseActivity > Cached preDownload $url")
                if (initNextVideoListener != null && currentDownloadingPos != -1) {
                    initNextVideoListener.onInitNextCall(currentDownloadingPos)
                    currentDownloadingPos = -1
                }
            } else {
                Logger.print("BaseActivity > Caching preDownload $url")
                httpProxyCacheServer?.registerCacheListener(cacheListener, url)
                preDownloadAsync = PreDownload()
                preDownloadAsync?.execute(url)
            }
        }
    }*/

    /*fun stopPreDownloading() {
        if (currentDownloadingPos > -1) {
            initNextVideoListener = null
            if (preDownloadAsync != null) {
                preDownloadAsync?.cancel(true)
            }
            httpProxyCacheServer?.unregisterCacheListener(cacheListener)
            currentDownloadingPos = -1
        }
    }*/

    fun prepareFilterList(): List<FilterModel> {
        val filterNames = resources?.getStringArray(R.array.filters)!!
        val arrayListFilter = ArrayList<FilterModel>()
        for (i in filterNames.indices) {
            var filterModel: FilterModel? = null
            when (i) {
                0 -> {
                    filterModel =
                        FilterModel(FilterType.DEFAULT, filterNames[i], R.drawable.default_filter)
                }
                1 -> {
                    filterModel = FilterModel(
                        FilterType.BILATERAL_BLUR,
                        filterNames[i],
                        R.drawable.bilateral_blur
                    )
                }
                2 -> {
                    filterModel =
                        FilterModel(FilterType.BOX_BLUR, filterNames[i], R.drawable.box_blur)
                }
                3 -> {
                    filterModel =
                        FilterModel(FilterType.BRIGHTNESS, filterNames[i], R.drawable.brightness)
                }
                4 -> {
                    filterModel = FilterModel(
                        FilterType.BULGE_DISTORTION,
                        filterNames[i],
                        R.drawable.bulge_distortion
                    )
                }
                5 -> {
                    filterModel = FilterModel(
                        FilterType.CGA_COLORSPACE,
                        filterNames[i],
                        R.drawable.cga_colorspace
                    )
                }
                6 -> {
                    filterModel =
                        FilterModel(FilterType.CONTRAST, filterNames[i], R.drawable.contrast)
                }
                7 -> {
                    filterModel =
                        FilterModel(FilterType.CROSSHATCH, filterNames[i], R.drawable.cross_hatch)
                }
                8 -> {
                    filterModel =
                        FilterModel(FilterType.EXPOSURE, filterNames[i], R.drawable.exposure)
                }
                9 -> {
                    filterModel = FilterModel(
                        FilterType.FILTER_GROUP_SAMPLE,
                        filterNames[i],
                        R.drawable.filter_group
                    )
                }
                10 -> {
                    filterModel = FilterModel(FilterType.GAMMA, filterNames[i], R.drawable.gamma)
                }
                11 -> {
                    filterModel = FilterModel(
                        FilterType.GAUSSIAN_FILTER,
                        filterNames[i],
                        R.drawable.gaussian_filter
                    )
                }
                12 -> {
                    filterModel =
                        FilterModel(FilterType.GRAY_SCALE, filterNames[i], R.drawable.gray_scale)
                }
                13 -> {
                    filterModel =
                        FilterModel(FilterType.HALFTONE, filterNames[i], R.drawable.half_tone)
                }
                14 -> {
                    filterModel = FilterModel(FilterType.HAZE, filterNames[i], R.drawable.haze)
                }
                15 -> {
                    filterModel = FilterModel(
                        FilterType.HIGHLIGHT_SHADOW,
                        filterNames[i],
                        R.drawable.highlight_shadow
                    )
                }
                16 -> {
                    filterModel = FilterModel(FilterType.HUE, filterNames[i], R.drawable.hue)
                }
                17 -> {
                    filterModel = FilterModel(FilterType.INVERT, filterNames[i], R.drawable.invert)
                }
                18 -> {
                    filterModel = FilterModel(
                        FilterType.LOOK_UP_TABLE_SAMPLE,
                        filterNames[i],
                        R.drawable.look_up_table
                    )
                }
                19 -> {
                    filterModel =
                        FilterModel(FilterType.LUMINANCE, filterNames[i], R.drawable.luminance)
                }
                20 -> {
                    filterModel = FilterModel(
                        FilterType.LUMINANCE_THRESHOLD,
                        filterNames[i],
                        R.drawable.threshold
                    )
                }
                21 -> {
                    filterModel =
                        FilterModel(FilterType.MONOCHROME, filterNames[i], R.drawable.monochrome)
                }
                22 -> {
                    filterModel =
                        FilterModel(FilterType.OPACITY, filterNames[i], R.drawable.opacity)
                }
                23 -> {
                    filterModel =
                        FilterModel(FilterType.PIXELATION, filterNames[i], R.drawable.pixelation)
                }
                24 -> {
                    filterModel =
                        FilterModel(FilterType.POSTERIZE, filterNames[i], R.drawable.posterize)
                }
                25 -> {
                    filterModel = FilterModel(FilterType.RGB, filterNames[i], R.drawable.rgb)
                }
                26 -> {
                    filterModel =
                        FilterModel(FilterType.SATURATION, filterNames[i], R.drawable.saturation)
                }
                27 -> {
                    filterModel = FilterModel(FilterType.SEPIA, filterNames[i], R.drawable.sepia)
                }
                28 -> {
                    filterModel = FilterModel(FilterType.SHARP, filterNames[i], R.drawable.sharp)
                }
                29 -> {
                    filterModel =
                        FilterModel(FilterType.SOLARIZE, filterNames[i], R.drawable.solarize)
                }
                30 -> {
                    filterModel = FilterModel(
                        FilterType.SPHERE_REFRACTION,
                        filterNames[i],
                        R.drawable.sphere_refraction
                    )
                }
                31 -> {
                    filterModel = FilterModel(FilterType.SWIRL, filterNames[i], R.drawable.swirl)
                }
                32 -> {
                    filterModel = FilterModel(FilterType.TONE, filterNames[i], R.drawable.tone)
                }
                33 -> {
                    filterModel = FilterModel(
                        FilterType.TONE_CURVE_SAMPLE,
                        filterNames[i],
                        R.drawable.tone_curve
                    )
                }
                34 -> {
                    filterModel =
                        FilterModel(FilterType.VIBRANCE, filterNames[i], R.drawable.vibrance)
                }
                35 -> {
                    filterModel =
                        FilterModel(FilterType.VIGNETTE, filterNames[i], R.drawable.vignette)
                }
                36 -> {
                    filterModel =
                        FilterModel(FilterType.WEAK_PIXEL, filterNames[i], R.drawable.weak_pixel)
                }
                37 -> {
                    filterModel = FilterModel(
                        FilterType.WHITE_BALANCE,
                        filterNames[i],
                        R.drawable.white_balance
                    )
                }
                38 -> {
                    filterModel =
                        FilterModel(FilterType.ZOOM_BLUR, filterNames[i], R.drawable.zoom_blur)
                }
            }
            arrayListFilter.add(filterModel!!)
        }
        return arrayListFilter
    }

    companion object {
        private var mediaPlayer = MediaPlayer()
        private var isPlayingAudio = false
        var forYouListResponse = ""

        private var httpProxyCacheServer: HttpProxyCacheServer? = null
        /*private var httpProxyCacheServerSec: HttpProxyCacheServer? = null
        private var httpProxyCacheServerPlayer: HttpProxyCacheServer? = null*/

        fun getProxy(context: Context): HttpProxyCacheServer? {
            if (httpProxyCacheServer == null) {
                httpProxyCacheServer = HttpProxyCacheServer.Builder(context).build()
            }
            return httpProxyCacheServer
        }

        /*fun getProxySec(context: Context): HttpProxyCacheServer? {
            if(httpProxyCacheServerSec == null) {
                httpProxyCacheServerSec = HttpProxyCacheServer.Builder(context).build()
            }
            return httpProxyCacheServerSec
        }

        fun getProxyPlayer(context: Context): HttpProxyCacheServer? {
            if(httpProxyCacheServerPlayer == null) {
                httpProxyCacheServerPlayer = HttpProxyCacheServer.Builder(context).build()
            }
            return httpProxyCacheServerPlayer
        }*/
        fun playAudio(audioUrl: String, onPrepareListener: OnPrepareListener) {
            stopAudio()
            isPlayingAudio = true
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                it.start()
                onPrepareListener.onPrepared()
            }

            mediaPlayer.setOnCompletionListener {
                onPrepareListener.onCompleted()
            }
        }


        fun cancelPreparingAudio() {
            mediaPlayer.setOnPreparedListener {}
        }

        fun stopAudio() {
            if (isPlayingAudio && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
                isPlayingAudio = false
            }
        }

        /*private var preDownloadAsync: PreDownload? = null
        class PreDownload : AsyncTask<String, Void, Void>() {
            private var inputStream: InputStream? = null

            override fun onPreExecute() {
                super.onPreExecute()
                Logger.print("PreDownload > onPreExecute ${System.currentTimeMillis()}")
            }

            override fun doInBackground(vararg params: String?): Void? {
                try {
                    val urlOfVideo = URL(httpProxyCacheServer!!.getProxyUrl(params[0]))
                    inputStream = urlOfVideo.openStream()
                    val bufferSize = 1024
                    val buffer = ByteArray(bufferSize)
                    var length = 0
                    while (inputStream?.read(buffer).also { length = it!! } != -1) {
                        if (inputStream == null || isCancelled) {
                            break
                        }
                    }
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    inputStream?.close()
                }
                return null
            }

            override fun onCancelled() {
                try {
                    inputStream?.close()
                    inputStream = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Logger.print("PreDownload > onCancelled ${System.currentTimeMillis()}")
                super.onCancelled()
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                Logger.print("PreDownload > onPostExecute ${System.currentTimeMillis()}")
            }
        }*/
    }
}