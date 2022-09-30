package smylee.app.ui.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlFilter
import com.daasuu.gpuv.egl.filter.GlFilterGroup
import com.daasuu.gpuv.egl.filter.GlMonochromeFilter
import com.daasuu.gpuv.egl.filter.GlVignetteFilter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_preview.*
import smylee.app.CallBacks.CopyFileListner
import smylee.app.MusicListing.MusicListingForYouAlbumActivity
import smylee.app.R
import smylee.app.adapter.FilterListAdapter
import smylee.app.camerfilters.FilterType
import smylee.app.camerfilters.OnFilterSelectListener
import smylee.app.custom.CustomVideoView
import smylee.app.dialog.CommonAlertDialog
import smylee.app.listener.OnPrepareListener
import smylee.app.model.FilterModel
import smylee.app.startPost.StartNewPostActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


open class PreviewActivity : BaseActivity(), View.OnClickListener, OnFilterSelectListener,
    OnPrepareListener, CopyFileListner {

    private var audioFileNameFinal: String = ""
    private var isDownloadFinal: String = ""
    private var ISAUDIOMIX: Boolean = false
    private var extractAudio: Boolean = false
    private var FINALaUDIOiD: String = ""
    private val requestTrimmerVideo = 106

    // private var player: MediaPlayer? = MediaPlayer()
    private var player: MediaPlayer? = null
    private var videoMediaPlayer: MediaPlayer? = null
    private var currentPlayerTime: Int = 0

    private lateinit var filterListAdapter: FilterListAdapter
    private var gpuMp4Composer: GPUMp4Composer? = null
    private var fileName = ""
    private var fileNameCamera = ""
    private var originalFile = ""
    private var originalFileCamera = ""
    private var originalFileCameraMerge = ""
    private var outPutFile = ""
    private var shareUri: Uri? = null
    private var stopPosition = 0
    private var copyFileListner: CopyFileListner? = null

    private val TAG = "PreviewActivity"
    private var glFilter: GlFilter = GlFilterGroup(GlMonochromeFilter(), GlVignetteFilter())
    private var isApplyFilter = false
    private var isFirstTime = true

    var IS_AUDIO_CAMERA: Boolean = false
    private var IS_MERGE: Boolean = false
    private var IS_CAMERA_PREVIEW: Boolean = false
    private var videoRotation: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        player = MediaPlayer()
        copyFileListner = this
        if (intent != null && intent?.action != Intent.ACTION_SEND) {
            IS_AUDIO_CAMERA = intent.getBooleanExtra("IS_AUDIO_CAMERA", false)
            IS_MERGE = intent.getBooleanExtra("IS_MERGE", false)
            IS_CAMERA_PREVIEW = intent.getBooleanExtra("IS_CAMERA_PREVIEW", false)
            extractAudio = intent.getBooleanExtra("extractAudio", false)
            videoRotation = intent.getIntExtra("videoRotation", 0)
            audioFileNameFinal = intent.getStringExtra("audioFile")!!
            shareUri = intent.getParcelableExtra("shareUri")
            Logger.print("audioFileNameFinal **************************$audioFileNameFinal")
            Logger.print("IS_AUDIO_CAMERA **************************$IS_AUDIO_CAMERA")
            Logger.print("IS_CAMERA_PREVIEW **************************$IS_CAMERA_PREVIEW")
            Logger.print("extractAudio **************************$extractAudio")
            if (audioFileNameFinal != "") {
                ISAUDIOMIX = true
            }
            Logger.print("ISAUDIOMIX **************************$ISAUDIOMIX")
        }



        mVideoView.setOnCompletionListener {
            Logger.print("mVideoView ********************")
        }


//        when {
//            intent?.action == Intent.ACTION_SEND -> {
//                if ("video/*" == intent.type) {
//                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
//
//                        Logger.print("get data from other APPS =========${it.path}")
//                        val path = FileUtils.getPathFromUri(this, it)
//                        //fileName(path)
//
//                        if (SharedPreferenceUtils.getInstance(this)
//                                .getBoolanValue(Constants.IS_LOGIN, false)
//                        ) {
//                            requestPermission(path)
//
//                        } else {
//                            val intent = Intent(this, LoginActivity::class.java)
//                            intent.putExtra("screen_name", "preview")
//                            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
//                        }
//                    }
//                }
//
//            }
//
//        }
        previewActivity = this
        Logger.print("IS_MERGE &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&$IS_MERGE")
        if (shareUri != null && shareUri!!.path != "") {
            val path = FileUtils.getPathFromUri(this, shareUri)
            Logger.print("path of shareUri=============$path")
            requestPermission(path)

        } else {
            if (IS_CAMERA_PREVIEW) {
                ivAudioSelect.visibility = View.GONE
                imgFilters.visibility = View.GONE

                if (IS_AUDIO_CAMERA && IS_MERGE) {
                    Handler().postDelayed({
                        playMergedFileCamera()
                    }, 800)
                } else if (IS_AUDIO_CAMERA) {
                    Handler().postDelayed({
                        playRecordedFile()
                    }, 800)
                } else if (IS_MERGE) {
                    Handler().postDelayed({
                        playMergedFileCamera()
                    }, 800)
                } else {
                    Handler().postDelayed({
                        playMergedFile()
                    }, 800)
                }
            } else {
                ivAudioSelect.visibility = View.VISIBLE
                imgFilters.visibility = View.VISIBLE
                Handler().postDelayed({
                    playMergedFile()
                }, 500)
            }
        }


        outPutFile = getVideoFilePath()!!
        mPlayVideo.setOnClickListener(this)
        ivAudioSelect.setOnClickListener(this)
        tvDonePreview.setOnClickListener(this)
        mVideoView.setOnClickListener(this)
        mpause.setOnClickListener(this)
        ivAudioRemove.setOnClickListener(this)
        llSend.setOnClickListener(this)
        imgFilters.setOnClickListener {
            if (rlFilters.visibility == View.VISIBLE) {
                rlFilters.visibility = View.GONE
            } else {
                rlFilters.visibility = View.VISIBLE
            }
        }

        imgCloseFilter.setOnClickListener {
            rlFilters.visibility = View.GONE
        }
        imgApplyFilter.setOnClickListener {
            stopMediaPlayer()
            rlFilters.visibility = View.GONE
            glFilter = FilterType.createGlFilter(FilterType.DEFAULT, applicationContext)
            prepareVideoFilter()
        }

        val filterTypes: List<FilterModel> = prepareFilterList()
        filterListAdapter = FilterListAdapter(this, filterTypes)
        filterListAdapter.setOnFilterSelectListener(this)
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvFilters.layoutManager = layoutManager
        rvFilters.adapter = filterListAdapter

        back.setOnClickListener {
            pauseMediaPlayer()
            finish()
        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.message_need_permission))
        builder.setMessage(getString(R.string.message_permission))
        builder.setPositiveButton(getString(R.string.title_go_to_setting)) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    private fun requestPermission(url: String) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                if (report.areAllPermissionsGranted()) {
                    Methods.deleteFilesFromDirectory(Constants.RECORD_PATH)


                    val file = File(url)
                    val retriever = MediaMetadataRetriever()
                    if (file != null && file.length() > 0) {
                        retriever.setDataSource(this@PreviewActivity, Uri.fromFile(file))
                    }
                    val METADATA_KEY_DURATION =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                    val seconds: Long =
                        TimeUnit.MILLISECONDS.toSeconds(METADATA_KEY_DURATION.toLong())

                    Logger.print("METADATA_KEY_DURATION=======================$seconds")


                    //  copyFile(url, "", Constants.RECORD_PATH, fileName(url))

                    if (seconds > 30) {
                        // Utils.showToastMessage(this@PreviewActivity, resources.getString(R.string.bigfilevalidation))
                        //finish()
                        if (file.exists()) {
                            val sizeInBytes = file.length()
                            val sizeInMb = sizeInBytes / (1024 * 1024)
                            Logger.print("fileSize!!!!!!!!!!$sizeInMb")
                            if (sizeInMb > 256) {
                                Utils.showToastMessage(
                                    this@PreviewActivity,
                                    resources.getString(R.string.bigfilevalidation)
                                )
                            } else {
                                Logger.print("file path error !!!!!!!!${file.absolutePath}")
                                val intent =
                                    Intent(this@PreviewActivity, TrimmerActivity::class.java)
                                if (file.name.length > 30) {
                                    Logger.print("file.name.length!!!!!!!!!!!${file.name.length}")
                                    var currentFileName: String =
                                        url.substring(url.lastIndexOf("/"), url.length)
                                    currentFileName = currentFileName.substring(1)

                                    Logger.print("currentFileName after substring!!!!!!!!!!!!!!!!$currentFileName")

                                    val directory =
                                        file.parent ?: Environment.getExternalStorageState()
                                    Logger.print("directory!!!!!!!!!!!!!!$directory")
                                    val from = File(directory, currentFileName)
                                    Logger.print("exist or not !!!!!!${from.parentFile?.exists()}")
                                    val to =
                                        File(directory, "Video_${System.currentTimeMillis()}_.mp4")
                                    try {
                                        if (rename(from, to)) {
                                            Logger.print("renamed sucessfully!!!!!!!!!!!")
                                        } else {
                                            Logger.print("error in renamed!!!!!!!!!!!")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    // from.renameTo(to)
                                    Logger.print("to absolute path !!!!!!!!!!!${to.absolutePath}")
                                    if (to.exists()) {
                                        intent.putExtra(
                                            CameraVideoRecording.EXTRA_VIDEO_PATH,
                                            to.absolutePath
                                        )
                                        Logger.print("currentFileName!!!!!!!!${to.absolutePath}")
                                        intent.putExtra("trimSec", 30)
                                        this@PreviewActivity.startActivityForResult(
                                            intent,
                                            requestTrimmerVideo
                                        )
                                    } else {
                                        Utils.showToastMessage(
                                            this@PreviewActivity,
                                            resources.getString(R.string.filevalidation)
                                        )
                                    }
                                } else {
                                    intent.putExtra(CameraVideoRecording.EXTRA_VIDEO_PATH, url)
                                    intent.putExtra("trimSec", 30)
                                    this@PreviewActivity.startActivityForResult(
                                        intent,
                                        requestTrimmerVideo
                                    )
                                }
                            }
                        } else {
                            Utils.showToastMessage(
                                this@PreviewActivity,
                                resources.getString(R.string.filevalidation)
                            )
                            Logger.print("file path error not exist!!!!!!!!${file.absolutePath}")
                        }


                    } else {
                        copyFileAsnc(
                            url,
                            "",
                            Constants.RECORD_PATH,
                            fileName(url),
                            copyFileListner!!
                        ).execute()
                        Logger.print("path other APPS====================$url")
                        Logger.print("fileName(path)====================${fileName(url)}")
                    }

                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    showSettingsDialog()
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        }).withErrorListener {
            Toast.makeText(this, "Error occurred! ", Toast.LENGTH_SHORT).show()
        }.onSameThread().check()
    }

    private fun rename(from: File, to: File): Boolean {
        return from.parentFile?.exists()!! && from.exists() && from.renameTo(to)
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

    private fun playMergedFile() {
        val folder = File(Constants.RECORD_PATH)
        Log.i(TAG, "Record files size ${folder.listFiles()!!.size}")
        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
            val filesInFolder = folder.listFiles()!!
            if (filesInFolder[0].length() <= 0) {
                filesInFolder[0].delete()
            } else {
                originalFile = filesInFolder[0].absolutePath
            }
        }
        Log.d("File_name", fileName)
        fileName = originalFile

        playVideoFromPath(fileName, false)
    }

    private fun playRecordedFile() {
        val folder = File(Constants.RECORD_PATH)
        Log.i(TAG, "Record files size ${folder.listFiles()!!.size}")
        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
            val filesInFolder = folder.listFiles()!!
            if (filesInFolder[0].length() <= 0) {
                filesInFolder[0].delete()
            } else {
                originalFileCamera = filesInFolder[0].absolutePath
            }
        }

        fileNameCamera = originalFileCamera
        Log.d("playRecordedFile", fileNameCamera)
        playVideoFromPath(fileNameCamera, false)
    }

    private fun copyFile(
        inputPath: String,
        inputFile: String,
        outputPath: String,
        oputPutFile: String
    ) {
        Logger.print("inputPath======================$inputPath")
        Logger.print("inputFile======================$inputFile")
        Logger.print("outputPath======================$outputPath")
        Logger.print("oputPutFile======================$oputPutFile")
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {

            //create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            `in` = FileInputStream(inputPath + inputFile)
            // out = FileOutputStream(outputPath + inputFile)
            out = FileOutputStream(outputPath + oputPutFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file (You have now copied the file)
            out.flush()
            out.close()
            out = null
        } catch (fnfe1: FileNotFoundException) {
            Log.e("tag", fnfe1.message)
        } catch (e: java.lang.Exception) {
            Log.e("tag", e.message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == requestTrimmerVideo) {
                if (resultCode == Activity.RESULT_OK) {
                    Handler().postDelayed({
                        playMergedFile()
                    }, 800)
                    /*
                    val intent = Intent(this, PreviewActivity::class.java)
                    intent.putExtra("audioFile", audioFileNameFinal)
                    intent.putExtra("IS_AUDIO_CAMERA", false)
                    intent.putExtra("IS_MERGE", false)
                    intent.putExtra("extractAudio", extractAudio)
                    intent.putExtra("shareUri", "")
                    intent.putExtra("IS_CAMERA_PREVIEW", false)
                    intent.putExtra("mStopVideo", false)
                    intent.putExtra("videoRotation", videoRotation)
                    startActivity(intent)*/
                }
            }
        }
    }

    class copyFileAsnc(
        var inputPath: String,
        var inputFile: String,
        var outputPath: String,
        var oputPutFile: String, var copyFileListner: CopyFileListner

    ) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg p0: Void?): Void? {
            copyFile(inputPath, inputFile, outputPath, oputPutFile)
            return null
        }

        private fun copyFile(
            inputPath: String,
            inputFile: String,
            outputPath: String,
            oputPutFile: String
        ) {
            Logger.print("copyFile======================")
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {

                //create output directory if it doesn't exist
                val dir = File(outputPath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                `in` = FileInputStream(inputPath + inputFile)
                // out = FileOutputStream(outputPath + inputFile)
                out = FileOutputStream(outputPath + oputPutFile)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                `in`.close()
                `in` = null

                // write the output file (You have now copied the file)
                out.flush()
                out.close()
                out = null
            } catch (fnfe1: FileNotFoundException) {
                Log.e("tag", fnfe1.message)
            } catch (e: java.lang.Exception) {
                Log.e("tag", e.message)
            }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            copyFileListner.onCopyFileCompleted(true)
        }


    }

    private fun playMergedFileCamera() {
        val folder = File(Constants.MERGE_PATH)
        Log.i(TAG, "MErged files size ${folder.listFiles()!!.size}")
        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
            val filesInFolder = folder.listFiles()!!
            if (filesInFolder[0].length() <= 0) {
                filesInFolder[0].delete()
            } else {
                originalFileCameraMerge = filesInFolder[0].absolutePath
            }
        }

        fileNameCamera = originalFileCameraMerge
        Log.d("playMergedFileCamera", fileNameCamera)
        playVideoFromPath(fileNameCamera, false)
    }

    private fun showDialogForRemoveAudio(
        ivAudioRemove1: AppCompatImageView,
        ivAudioSelect1: AppCompatImageView
    ) {
        val alertDialog =
            object : CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    audioFileNameFinal = ""
                    FINALaUDIOiD = ""
                    ISAUDIOMIX = false
                    MusicListingForYouAlbumActivity.final_Audio_File = ""
                    MusicListingForYouAlbumActivity.AUDIO_ID = ""
                    MusicListingForYouAlbumActivity.audioDuration = 0L
                    MusicListingForYouAlbumActivity.is_Download = ""

                    ivAudioRemove1.visibility = View.GONE
                    ivAudioSelect1.visibility = View.VISIBLE
                }

                override fun cancelClicked() {
                }
            }
        alertDialog.initDialog(
            resources.getString(R.string.ok),
            resources.getString(R.string.remove_audio)
        )
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun playVideoFromPath(filePath: String, isResume: Boolean) {
        Logger.print("playVideoFromPath !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$filePath")

        if (!filePath.contentEquals("")) {
            Logger.print("playVideoFromPath filePath!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$filePath")
            if (mPlayVideo.visibility == View.GONE) {
                mPlayVideo.visibility = View.VISIBLE
            }

            //mVideoView!!.setMediaController(MediaController(this))
            mVideoView!!.setMediaController(null)
            mVideoView!!.requestFocus()
            mVideoView!!.setVideoURI(Uri.fromFile(File(filePath)))
            mVideoView!!.seekTo(100)
            mVideoView!!.setOnPreparedListener {
                videoMediaPlayer = it
                //mVideoView!!.start()
            }
            Logger.print("ISAUDIOMIX IN playVideoFromPath ***************$ISAUDIOMIX")
            Logger.print("IS_CAMERA_PREVIEW IN playVideoFromPath ***************$IS_CAMERA_PREVIEW")
            if (ISAUDIOMIX && !IS_CAMERA_PREVIEW) {
                if (extractAudio && !isResume && !isApplyFilter) {
                    showDialogForApplyExtractedAudio(ivAudioRemove, ivAudioSelect)
                }
            }
            mVideoView!!.setOnCompletionListener {
                Logger.print("setOnCompletionListener preview !!!!!!!!!!!!!")
                if (mPlayVideo.visibility == View.GONE) {
                    mPlayVideo.visibility = View.VISIBLE
                }
                if (ISAUDIOMIX || IS_AUDIO_CAMERA) {
                    stopMediaPlayer()
                }
            }
            mVideoView.setPlayPauseListener(object : CustomVideoView.PlayPauseListener {
                override fun onPlay() {
                    if (ISAUDIOMIX || IS_AUDIO_CAMERA && currentPlayerTime > 0) {
                        resumeMediaPlayer()
                    }
                }

                override fun onPause() {
                    if (ISAUDIOMIX || IS_AUDIO_CAMERA) {
                        pauseMediaPlayer()
                    }
                }

                override fun onComPlete() {
                    println("setOnCompletionListener!!!!!!!!!!!!!")
                }
            })
        }
    }

    private fun prepareVideoFilter() {
        gpuMp4Composer = null
        val output = File(outPutFile)
        if (output.exists()) {
            output.delete()
        }
        showProgressDialog()
        gpuMp4Composer = GPUMp4Composer(fileName, outPutFile) // .rotation(Rotation.ROTATION_270)
            .filter(glFilter)
            .mute(false)
            .flipHorizontal(false)
            .flipVertical(false)
            .listener(object : GPUMp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    Log.d(TAG, "onProgress = $progress")
                }

                override fun onCompleted() {
                    hideProgressDialog()
                    Log.d(TAG, "onCompleted()")
                    exportMp4ToGallery(applicationContext, outPutFile)
                    isApplyFilter = true
                    runOnUiThread {
                        playVideoFromPath(outPutFile, false)
                    }
                }

                override fun onCanceled() {}
                override fun onFailed(exception: Exception) {
                    Log.d(TAG, "onFailed()")
                }
            }).start()
    }

    open fun exportMp4ToGallery(context: Context, filePath: String) {
        val values = ContentValues(2)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, filePath)
        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://$filePath")
            )
        )
    }

    private fun stopMediaPlayer() {
        if (player != null) {
            player?.stop()
            player?.release()
            player = null
        }
    }

    private fun pauseMediaPlayer() {
        if (player != null && player?.isPlaying!!) {
            player?.pause()
            currentPlayerTime = player?.currentPosition!!
        }
    }

    private fun resumeMediaPlayer() {
        if (player != null && !player?.isPlaying!!) {
            player?.seekTo(currentPlayerTime)
            player?.start()
        }
    }

    private fun setUpMediaPlayer(path_audio: String) {
        Logger.print("setUpMediaPlayer!!!!!!!!$path_audio")
        try {
            player = MediaPlayer()
            player?.setAudioStreamType(AudioManager.ADJUST_LOWER)
            player?.reset()
            player?.setDataSource(path_audio)
            player?.isLooping = true
            player?.prepare()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            println("Exception of type : $e")
            e.printStackTrace()
        }
        player?.start()
    }

    open fun getAndroidMoviesFolder(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    }

    open fun getVideoFilePath(): String? {
        val file = getAndroidMoviesFolder()
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + SimpleDateFormat(
            "yyyyMM_dd-HHmmss",
            Locale.getDefault()
        ).format(Date()) + "filter_apply.mp4"
    }

    override fun onStop() {
        pauseMediaPlayer()
        super.onStop()
    }

    override fun onPause() {
        pauseMediaPlayer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstTime) {
            isFirstTime = false
        } else {
            Logger.print("outPutFile onResume!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$outPutFile")
            if (mpause != null && mpause.visibility == View.VISIBLE) {
                mpause.visibility = View.GONE
            }
            mVideoView.stopPlayback()
            if (shareUri != null && shareUri!!.path != "" && isApplyFilter && outPutFile != "") {
                Logger.print("outPutFile !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$outPutFile")
                playVideoFromPath(outPutFile, true)
            } else if (shareUri != null && shareUri!!.path != "") {
                Logger.print("shareUri onResume =================$shareUri")
                Handler().postDelayed({
                    playMergedFile()
                }, 800)
            } else if (isApplyFilter && outPutFile != "") {
                Logger.print("outPutFile !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$outPutFile")
                playVideoFromPath(outPutFile, true)
            } else if (fileName != "") {
                Logger.print("fileName !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$fileName")
                playVideoFromPath(fileName, true)
            } else if (fileNameCamera != "") {
                Logger.print("fileNameCamera !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$fileNameCamera")
                playVideoFromPath(fileNameCamera, true)
            } else if (outPutFile != "") {
                Logger.print("outPutFile !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!$outPutFile")
                playVideoFromPath(outPutFile, true)
            }
        }

        // if (!IS_CAMERA_PREVIEW && !ISAUDIOMIX) {
        if (!IS_CAMERA_PREVIEW) {
            if (MusicListingForYouAlbumActivity.final_Audio_File != "") {
                audioFileNameFinal = MusicListingForYouAlbumActivity.final_Audio_File
                if (mpause.visibility == View.VISIBLE) {
                    mpause.visibility = View.GONE
                }
                if (mPlayVideo.visibility == View.GONE) {
                    mPlayVideo.visibility = View.VISIBLE
                }
            }
            if (MusicListingForYouAlbumActivity.is_Download != "") {
                isDownloadFinal = MusicListingForYouAlbumActivity.is_Download
            }
            if (MusicListingForYouAlbumActivity.AUDIO_ID != "") {
                FINALaUDIOiD = MusicListingForYouAlbumActivity.AUDIO_ID
            }
            Logger.print("final_Audio_File onresume preview==========$audioFileNameFinal")
        }

        if (!IS_AUDIO_CAMERA) {
            if (!audioFileNameFinal.contentEquals("")) {
                ivAudioSelect.visibility = View.GONE
                ivAudioRemove.visibility = View.VISIBLE
                ISAUDIOMIX = true

                if (mVideoView != null && !mVideoView.isPlaying) {
                    mVideoView!!.seekTo(100)
                }
            } else {
                ivAudioRemove.visibility = View.GONE
                //ivAudioSelect.visibility = View.VISIBLE
                ISAUDIOMIX = false
            }
        }
    }

    override fun onBackPressed() {
        pauseMediaPlayer()
        Logger.print("onBackPressed audioFileNameFinal!!!!!!!!$audioFileNameFinal")
        Logger.print("onBackPressed isDownload!!!!!!!!$isDownloadFinal")
        val intent = Intent()
        intent.putExtra("audioFileNameFinal", audioFileNameFinal)
        intent.putExtra("isDownload", isDownloadFinal)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAudioSelect -> {
                mPlayVideo!!.visibility = View.VISIBLE
                stopMediaPlayer()
                val intent = Intent(this, MusicListingForYouAlbumActivity::class.java)
                this.startActivity(intent)
            }

            R.id.tvDonePreview -> {
                if (IS_CAMERA_PREVIEW) {
                    if (mVideoView.isPlaying) {
                        mVideoView.stopPlayback()
                    }
                    if (IS_AUDIO_CAMERA) {
                        stopMediaPlayer()
                    }
                    val intent = Intent()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    if (mVideoView.isPlaying) {
                        mVideoView.stopPlayback()
                    }
                    if (ISAUDIOMIX) {
                        stopMediaPlayer()
                    }
                    if (isApplyFilter) {
                        isApplyFilter = false
                        val folder = File(Constants.RECORD_PATH)
                        Log.i(TAG, "Record files size ${folder.listFiles()!!.size}")
                        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
                            val filesInFolder = folder.listFiles()!!
                            filesInFolder[0].delete()
                            val file =
                                File(folder, "FilterApply_" + System.currentTimeMillis() + ".mp4")
                            file.createNewFile()
                            fileName = ""
                            CopyFileAsync(this@PreviewActivity).execute("$outPutFile;${file.absolutePath}")
                        }
                    } else {
                        val intent = Intent()
                        intent.putExtra("audioFileNameFinal", audioFileNameFinal)
                        intent.putExtra("isDownload", isDownloadFinal)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }


            R.id.ivAudioRemove -> {
                showDialogForRemoveAudio(ivAudioRemove, ivAudioSelect)
            }

            R.id.mPlayVideo -> {
                Logger.print("ISAUDIOMIX !!!!!!$ISAUDIOMIX")
                Logger.print("IS_AUDIO_CAMERA !!!!!!$IS_AUDIO_CAMERA")
                if (ISAUDIOMIX || IS_AUDIO_CAMERA) {
                    if (!audioFileNameFinal.contentEquals("")) {
                        Logger.print("audioFileNameFinal !!!!!!$audioFileNameFinal")
                        val audioFilePath = Constants.Audio_PATH + audioFileNameFinal
                        setUpMediaPlayer(audioFilePath)
                    }
                    videoMediaPlayer?.setVolume(0.0f, 0.0f)
                }
                mVideoView.start()
                if (!IS_CAMERA_PREVIEW) {
                    ivAudioSelect.visibility = View.VISIBLE
                    ivAudioRemove!!.visibility = View.GONE
                }
                mPlayVideo!!.visibility = View.GONE
            }
            R.id.mVideoView -> {
                if (mVideoView.isPlaying) {
                    mVideoView.pause()
                    stopPosition = mVideoView.currentPosition
                    Logger.print("stopPosition==================$stopPosition")
                    if (ISAUDIOMIX) {
                        pauseMediaPlayer()
                    }
                    if (IS_AUDIO_CAMERA) {
                        pauseMediaPlayer()
                    }
                    mpause!!.visibility = View.VISIBLE
                }
            }
            R.id.mpause -> {
                mVideoView.seekTo(stopPosition)
                mVideoView.start()
                if (ISAUDIOMIX) {
                    resumeMediaPlayer()
                }
                if (IS_AUDIO_CAMERA) {
                    resumeMediaPlayer()
                }
                mpause!!.visibility = View.GONE
            }

            R.id.llSend -> {
                if (IS_CAMERA_PREVIEW) {
                    Logger.print("IS_AUDIO_CAMERA!!!!!!!!!!!!!!!!!$IS_AUDIO_CAMERA")
                    if (mVideoView.isPlaying) {
                        mVideoView.stopPlayback()
                    }
                    if (IS_AUDIO_CAMERA) {
                        stopMediaPlayer()
                    }
                    val intent = Intent(this, StartNewPostActivity::class.java)
                    intent.putExtra("isFromPickGallery", false)
                    intent.putExtra("audioFileNameFinal", audioFileNameFinal)
                    intent.putExtra("isDownload", isDownloadFinal)
                    intent.putExtra("finalAudioId", FINALaUDIOiD)
                    intent.putExtra("IS_MERGE_FILE", IS_MERGE)
                    intent.putExtra("videoRotation", videoRotation)
                    startActivity(intent)
                } else {
                    if (mVideoView!!.isPlaying) {
                        mVideoView!!.stopPlayback()
                    }
                    if (ISAUDIOMIX) {
                        stopMediaPlayer()
                    }
                    if (isApplyFilter) {
                        isApplyFilter = false
                        val folder = File(Constants.RECORD_PATH)
                        Log.i(TAG, "Record files size ${folder.listFiles()!!.size}")
                        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
                            val filesInFolder = folder.listFiles()!!
                            filesInFolder[0].delete()
                            val file =
                                File(folder, "FilterApply_" + System.currentTimeMillis() + ".mp4")
                            file.createNewFile()
                            fileName = ""
                            CopyFileAsync(this@PreviewActivity).execute("$outPutFile;${file.absolutePath}")
                        }
                    } else {
                        val intent = Intent(this, StartNewPostActivity::class.java)
                        intent.putExtra("isFromPickGallery", true)
                        intent.putExtra("audioFileNameFinal", audioFileNameFinal)
                        intent.putExtra("isDownload", isDownloadFinal)
                        intent.putExtra("finalAudioId", FINALaUDIOiD)
                        intent.putExtra("IS_MERGE_FILE", IS_MERGE)
                        intent.putExtra("videoRotation", videoRotation)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showDialogForApplyExtractedAudio(
        ivAudioRemove1: AppCompatImageView,
        ivAudioSelect1: AppCompatImageView
    ) {
        val alertDialog =
            object : CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    ISAUDIOMIX = true
                    ivAudioSelect1.visibility = View.VISIBLE

                    if (mVideoView != null && !mVideoView.isPlaying) {
                        mVideoView!!.seekTo(100)
                    }
                }

                override fun cancelClicked() {
                    ISAUDIOMIX = false
                    IS_AUDIO_CAMERA = false
                    audioFileNameFinal = ""
                    ivAudioSelect1.visibility = View.VISIBLE
                    if (ivAudioRemove1.visibility == View.VISIBLE) {
                        ivAudioRemove1.visibility = View.GONE
                    }
                }
            }
        alertDialog.initDialog(
            resources.getString(R.string.ok),
            resources.getString(R.string.audioExtract)
        )
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    override fun onFilterSelected(position: Int, filterType: FilterType) {
        rlFilters.visibility = View.GONE
        glFilter = FilterType.createGlFilter(filterType, applicationContext)
        stopMediaPlayer()

        if (mVideoView != null && mVideoView.isPlaying) {
            mVideoView.pause()
        }
        prepareVideoFilter()
    }

    class CopyFileAsync(private val onPrepareListener: OnPrepareListener) :
        AsyncTask<String?, Void?, String?>() {

        override fun doInBackground(vararg params: String?): String? {
            val ars = params[0]?.split(";".toRegex())?.toTypedArray()
            val file = File(ars!![0])
            val fileDest = File(ars[1])

            try {
                val `in`: FileInputStream? = FileInputStream(file)
                val out: FileOutputStream? = FileOutputStream(fileDest)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`!!.read(buffer).also { read = it } != -1) {
                    out!!.write(buffer, 0, read)
                }
                `in`.close()

                // write the output file
                out!!.flush()
                out.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return fileDest.absolutePath
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //   fileName = ""
            onPrepareListener.onCompleted()
        }
    }

    override fun onPrepared() {
    }

    override fun onCompleted() {
        Logger.print("onCompleted !!!!!!!^^^^^^^^^^^^!!!!!!!!!!!!!!!!!!!!!!")
        val intent = Intent(this, StartNewPostActivity::class.java)
        intent.putExtra("isFromPickGallery", true)
        intent.putExtra("audioFileNameFinal", audioFileNameFinal)
        intent.putExtra("isDownload", isDownloadFinal)
        intent.putExtra("finalAudioId", FINALaUDIOiD)
        intent.putExtra("IS_MERGE_FILE", IS_MERGE)
        intent.putExtra("videoRotation", videoRotation)
        startActivity(intent)
    }

    companion object {
        var previewActivity: PreviewActivity? = null
    }

    override fun onCopyFileCompleted(sucess: Boolean) {
        Logger.print("onCopyFileCompleted ============")
        Handler().postDelayed({
            playMergedFile()
        }, 800)
    }
}