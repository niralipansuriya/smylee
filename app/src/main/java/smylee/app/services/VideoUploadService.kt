package smylee.app.services

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.media.*
import android.media.MediaMetadataRetriever.OPTION_CLOSEST
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.firebase.analytics.FirebaseAnalytics
import com.urfeed.listener.OnResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import smylee.app.CallBacks.CompressCallBack
import smylee.app.CallBacks.MergerCallBacks
import smylee.app.R
import smylee.app.custom.AnimatedGifEncoder
import smylee.app.engine.VideoResolutionChanger
import smylee.app.home.HomeActivity
import smylee.app.listener.GifCompeleteListner
import smylee.app.postvideo.PostVideoRepository
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.utils.*
import java.io.*
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.log10
import kotlin.math.pow


class VideoUploadService : Service(), MergerCallBacks, CompressCallBack, GifCompeleteListner {

    private var currentStateOfService = "Preparing video..."
    private var idsSpeed = ArrayList<String>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var isAudioMix: Boolean = false
    private var isFbPost: Boolean = false
    private var isInstaPost: Boolean = false
    private var isMergeFile: Boolean = false
    private var mixAudioDir: File? = null
    private var mStopVideo: Boolean = false
    private var audioFileNameFinal: String = ""
    private var finalAudioId: String = ""
    private var gifCompeleteListner: GifCompeleteListner? = null

    private var selectedCatIds = ""
    private var selectedLanguageIds = ""
    private var postTitle = ""

    private var videoFile: File? = null
    private var gifFile: File? = null
    private var thumbnailFile: File? = null

    private var thumbBitmap: Bitmap? = null
    private lateinit var pendingIntent: PendingIntent
    private var videoRotation = 0
    private var isFromPickGallery: Boolean = false
    private var isFileCurrupt: Boolean = false

    companion object {
        private const val channelId = "VideoUploadService"
        private const val TAG = "VideoUploadService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, HomeActivity::class.java)
        notificationIntent.putExtra("screenName", "")
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        gifCompeleteListner = this
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification: Notification?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(this, channelId)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(currentStateOfService)
                .setSmallIcon(R.drawable.ic_video_icon)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            notification = Notification(
                R.drawable.ic_video_icon,
                resources.getString(R.string.app_name),
                System.currentTimeMillis()
            )
            notification.contentIntent = pendingIntent
        }
        startForeground(1, notification)

        idsSpeed = CameraVideoRecording.IDS_speed
        mixAudioDir = Path.createFileDir(this, Constants.MixAudioVideo)
        if (intent != null) {
            isAudioMix = intent.getBooleanExtra("isAudioMix", false)
            isMergeFile = intent.getBooleanExtra("IS_MERGE_FILE", false)
            isFbPost = intent.getBooleanExtra("isFbPost", false)
            isInstaPost = intent.getBooleanExtra("isInstaPost", false)
            val audioFileNameFinal1 = intent.getStringExtra("audioFileName")!!
            finalAudioId = intent.getStringExtra("finalAudioId")!!
            audioFileNameFinal = Constants.Audio_PATH + audioFileNameFinal1
            Logger.print("audio_file_name_final===========$audioFileNameFinal")
            Logger.print("finalAudioId===========$finalAudioId")
            Logger.print("IS_MERGE_FILE video upload service===========$isMergeFile")

            mStopVideo = intent.getBooleanExtra("mStopVideo", false)
            selectedCatIds = intent.getStringExtra("selectedCatIds")!!
            selectedLanguageIds = intent.getStringExtra("selectedLanguageIds")!!
            postTitle = intent.getStringExtra("postTitle")!!

            Logger.print("postTitle in service=============$postTitle")
            videoRotation = intent.getIntExtra("videoRotation", 0)
            isFromPickGallery = intent.getBooleanExtra("isFromPickGallery", false)

            if (isMergeFile) {
                Logger.print("IS_MERGE_FILE if #################$isMergeFile")
                val path: String = Constants.MERGE_PATH
                Log.d("Files", "Path: $path")
                val directory = File(path)
                val files = directory.listFiles()
                var nameFile: String? = ""

                if (files != null && files.isNotEmpty()) {
                    for (file in directory.listFiles()!!) {
                        nameFile = file.absolutePath
                    }
                }

                val fileCompressed = File(nameFile!!)
                if (fileCompressed.length() <= 0) {
                    Logger.print("IS_MERGE_FILE fileCompressed.length() <= 0 #################$isMergeFile")
                    fileCompressed.delete()
                    deleteFiles()
                    sendMessageBroadcast(resources.getString(R.string.video_files_empty))
//                    Utils.showToastMessage(applicationContext, resources.getString(R.string.video_files_empty))
                    this@VideoUploadService.stopForeground(true)
                    stopSelf()
                } else {
                    Logger.print("IS_MERGE_FILE else  #################$isMergeFile")
                    Log.d("FileName : Speed == ", "" + nameFile)
                    currentStateOfService = resources.getString(R.string.compressing_video)
                    updateNotificationText()
                    if (idsSpeed.size == 0) {
                        if (isAudioMix) {
                            MixAudio(mixAudioDir!!, path, audioFileNameFinal, this).execute()
                        } else {
                            startCompressServiceForVideo(nameFile, mStopVideo)
                        }
                    }
                }
            } else {
                Logger.print("IS_MERGE_FILE else  Constants.RECORD_PATH #################$isMergeFile")
                val folder = File(Constants.RECORD_PATH)
                if (folder.listFiles()!!.size == 1) {
                    Logger.print("IS_MERGE_FILE else  Constants.RECORD_PATH size == 1 #################$isMergeFile")
                    val filesInFolder = folder.listFiles()!!
                    if (filesInFolder[0].length() <= 0) {
                        filesInFolder[0].delete()
                        deleteFiles()
                        sendMessageBroadcast(resources.getString(R.string.video_files_empty))
//                        Utils.showToastMessage(applicationContext,resources.getString(R.string.video_files_empty))
                        this@VideoUploadService.stopForeground(true)
                        stopSelf()
                    } else {
                        currentStateOfService = resources.getString(R.string.compressing_video)
                        updateNotificationText()
                        if (isAudioMix) {
                            MixAudio(
                                mixAudioDir!!,
                                folder.absolutePath,
                                audioFileNameFinal,
                                this
                            ).execute()
                        } else {
                            startCompressServiceForVideo(filesInFolder[0].absolutePath, mStopVideo)
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startCompressServiceForVideo(filePath: String, mStopVideo: Boolean) {
        Log.d("Call or not ", "YES CALL")
        val fileFromPath = File(filePath)
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {

                if (fileFromPath.exists() && fileFromPath.length() > 0) {
                    Logger.print("filePath========================$filePath")
                    Logger.print("fileFromPath========================${fileFromPath.length()}")

                    try {

                        val thumb = ThumbnailUtils.createVideoThumbnail(
                            filePath,
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
                        )!!
                        thumbBitmap = thumb
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val thumb = ThumbnailUtils.createVideoThumbnail(
                        filePath,
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
                    )!!
                    thumbBitmap = thumb
                    startCompressVideoProcess(fileFromPath, mStopVideo)
                }

                return null
            }
        }.execute()
    }

    private fun startCompressVideoProcess(filePAth: File, mStopVideoORNOT: Boolean) {
        try {
            val pathToReEncodedFile = VideoResolutionChanger(baseContext).changeResolution(
                filePAth, mStopVideoORNOT, !isFromPickGallery
            )
            val compressedFilePath = File(pathToReEncodedFile)
            Log.d("compressedFilePath", compressedFilePath.absolutePath + "")
            //final File compressedFilePath = new File(pathToReEncodedFile);
            Handler(Looper.getMainLooper()).post {
                Log.d("Compress Complete", compressedFilePath.absolutePath)
                Log.d("Compress File SIZE", fileSize(compressedFilePath.absoluteFile))
                sendCompletionBroadCast(false)
            }
        } catch (ex: Exception) {
            //showMessage(ex.message)
            if (mStopVideoORNOT) {
                println("call or not catch mStopVideoORNOT!!!!!!!!!!!!!!$mStopVideoORNOT")
            }
            ex.printStackTrace()
            Logger.print("startCompressVideoProcess !!!!!!!!!!!!!" + ex.message)
            Log.d(TAG, ex.message, ex)
            sendCompletionBroadCast(true)
            //sendCompletionBroadCast(false)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            //showMessage(throwable.message)
            if (mStopVideoORNOT) {
                println("call or not throwable mStopVideoORNOT!!!!!!!!!!!!!!$mStopVideoORNOT")
            }
            Logger.print("startCompressVideoProcess throwable !!!!!!!!!!!!!" + throwable.message)
            Log.d(TAG, throwable.message, throwable)
            //sendCompletionBroadCast(false)
            sendCompletionBroadCast(true)
        }
    }

    private fun fileSize(file: File): String {
        return readableFileSize(file.length())
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "$size B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return (DecimalFormat("#,##0.##").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups])
    }

    private fun sendCompletionBroadCast(isError: Boolean) {
        val path: String = if (isError) {
            when {
                isAudioMix -> {
                    Constants.MixAudioVideo_PATH
                }
                isMergeFile -> {
                    Constants.MERGE_PATH
                }
                else -> {
                    Constants.RECORD_PATH
                }
            }
        } else {
            Constants.COMPRESSEDFILES_PATH
        }

        var nameFile = ""
        val directory = File(path)
        val files = directory.listFiles()
        var durationFile: File? = null

        if (files != null && files.isNotEmpty() && files.isNotEmpty()) {
            for (file in directory.listFiles()!!) {
                durationFile = file
                nameFile = file.absolutePath
            }
            Logger.print("nameFile!!!!!!${nameFile.length}")
        }
        val retriever = MediaMetadataRetriever()
        var fileDuration = ""

        try {
            retriever.setDataSource(this, Uri.parse(nameFile))
            fileDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        Logger.print("fileDuration========================$fileDuration")
        if (fileDuration.contentEquals("") || fileDuration == null || fileDuration.contentEquals("null") || fileDuration.toInt() < 0) {
            isFileCurrupt = true
        }
        isFileCurrupt =
            path.contentEquals(Constants.COMPRESSEDFILES_PATH) && nameFile.isEmpty() && nameFile.length <= 0
        var finalVideoFile = ""
        val finalVideoPath: String
        if (isFileCurrupt) {
            finalVideoPath = when {
                isAudioMix -> {
                    Constants.MixAudioVideo_PATH
                }
                isMergeFile -> {
                    Constants.MERGE_PATH
                }
                else -> {
                    Constants.RECORD_PATH
                }
            }
            val finalDir = File(finalVideoPath)
            val filesInDir = finalDir.listFiles()

            if (filesInDir != null && filesInDir.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    durationFile = file
                    finalVideoFile = file.absolutePath
                }
                Logger.print("finalVideoFile!!!!!!${finalVideoPath.length}")
            }
        } else {
            finalVideoFile = nameFile
        }

        var seconds: Long = 0
        var width: Int = 0
        var height: Int = 0
        var timeDuration: String = ""
        //video duration
        try {
            val retriever = MediaMetadataRetriever()
            if (durationFile != null && durationFile.length() > 0) {
                retriever.setDataSource(this, Uri.fromFile(durationFile))
            }
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            timeDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            width =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            height =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

            val timeInMillisec = time.toLongOrNull() ?: 0
            Logger.print("timeInMillisec !!!!!!!!!!!!!$timeInMillisec")
            seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec)

            Logger.print("seconds !!!!!!!!!!!!$seconds")
            retriever.release()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

        Logger.print("catIDs in in broadcast============$selectedCatIds")
        Logger.print("postTitle in in broadcast============$postTitle")
        Logger.print("selectedLanguageIds in in broadcast============$selectedLanguageIds")
        Logger.print("finalVideoFilePath======$finalVideoFile")
        /* TaskSaveGIFFfmpeg(finalVideoFile,
             timeDuration,
             width,
             height,
             seconds,
             thumbBitmap!!,
             gifCompeleteListner!!).execute()*/
        TaskSaveGIF(
            finalVideoFile,
            timeDuration,
            width,
            height,
            seconds,
            thumbBitmap!!,
            gifCompeleteListner!!
        ).execute()
        //persistImage(selectedCatIds,selectedLanguageIds,thumbBitmap!!,finalVideoFile,postTitle,seconds)
    }

    private fun persistImage(
        iDS_cat: String, iDS_language: String, bitmap: Bitmap, video_file: String?,
        postTitle: String, videoDuration: Long
    ) {
        val videoWidth: Int
        val videoHeight: Int

        if (isFromPickGallery) {
            videoWidth = bitmap.width
            videoHeight = bitmap.height
        } else {
            videoWidth = 720
            videoHeight = 1280
        }

        currentStateOfService = resources.getString(R.string.uploading_video)
        updateNotificationText()
        Logger.print("iDS_cat========$iDS_cat")
        Logger.print("iDS_language=====$iDS_language")
        Logger.print("postTitle=====$postTitle")
        Logger.print("videoWidth=====$videoWidth")
        Logger.print("videoHeight=====$videoHeight")
        val multiBody: List<MultipartBody.Part>?
        val multiBodyThumb: List<MultipartBody.Part>?

        multiBody = java.util.ArrayList()
        multiBodyThumb = java.util.ArrayList()

        val filesDir: File = filesDir
        val imageFile = File(filesDir, "thumbnail.png")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 60, os)
            os.flush()
            os.close()

            Logger.print("THumb File=======" + imageFile.absolutePath)
            videoFile = File(video_file!!)
            var giffilename = Methods.filePathFromDir(Constants.GIF_Path)
            gifFile = File(giffilename!!)

            if (gifFile!!.exists() && gifFile!!.length() > 0) {

                val reqThumbFile = gifFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                val bodyThumb =
                    MultipartBody.Part.createFormData("video_thumb", gifFile!!.name, reqThumbFile)
                multiBodyThumb.add(bodyThumb)

            } else {
                thumbnailFile = imageFile

                if (thumbnailFile!!.exists()) {
                    val reqThumbFile = thumbnailFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                    val bodyThumb = MultipartBody.Part.createFormData(
                        "video_thumb",
                        thumbnailFile!!.name,
                        reqThumbFile
                    )
                    multiBodyThumb.add(bodyThumb)

                }

            }


            Logger.print("videoFile!!.name #############################" + videoFile!!.absolutePath)
            Logger.print("giffilename!!!!!!!!!!!!" + giffilename)

            val hashMap = HashMap<String, RequestBody>()
            hashMap["post_title"] = postTitle.toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["post_category_id"] = iDS_cat.toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["post_type"] = "2".toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["post_visibility"] = "1".toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["post_video_duration"] =
                videoDuration.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["language"] = iDS_language.toRequestBody("text/plain".toMediaTypeOrNull())
            hashMap["post_height"] = "$videoHeight".toRequestBody("text/plain".toMediaType())
            hashMap["post_width"] = "$videoWidth".toRequestBody("text/plain".toMediaType())
            if (!finalAudioId.contentEquals("")) {
                hashMap["audio_id"] = finalAudioId.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            Logger.print("finalAudioId=============$finalAudioId")
            val reqFile = videoFile!!.asRequestBody("video/*".toMediaTypeOrNull())
            // val reqThumbFile = thumbnailFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("video_post", videoFile!!.name, reqFile)
            // val bodyThumb = MultipartBody.Part.createFormData("video_thumb", thumbnailFile!!.name, reqThumbFile)
            multiBody.add(body)
            postVideo(hashMap, multiBody, multiBodyThumb)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
            sendMessageBroadcast("Error writing bitmap ${e.message}")
            this@VideoUploadService.stopForeground(true)
            stopSelf()
        }
    }

    private fun postVideo(
        hashMap: HashMap<String, RequestBody>, multiBody: List<MultipartBody.Part>?,
        multiBodyThumb: List<MultipartBody.Part>?
    ) {
        PostVideoRepository().postVideoWithCallback(
            applicationContext,
            hashMap,
            multiBody,
            multiBodyThumb,
            object : OnResponse {
                override fun onSuccess(code: Int, response: String) {
                    Log.i(TAG, "onSuccess $response")
                    val jsonObject = JSONObject(response)
                    SharedPreferenceUtils.getInstance(applicationContext)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    when (jsonObject.getInt("code")) {
                        1 -> {
                            sendMessageBroadcast(jsonObject.getString("message"))
                            deleteFiles()
                            this@VideoUploadService.stopForeground(true)
                            stopSelf()

                            val bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "NewPost")
                            firebaseAnalytics.logEvent(
                                FirebaseAnalytics.Event.SELECT_CONTENT,
                                bundle
                            )

                        }
                        0 -> {
                            deleteFiles()
                            sendMessageBroadcast(jsonObject.getString("message"))
                            this@VideoUploadService.stopForeground(true)
                            stopSelf()
                        }
                        else -> {
                            deleteFiles()
                            sendMessageBroadcast(jsonObject.getString("message"))
                            this@VideoUploadService.stopForeground(true)
                            stopSelf()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.i(TAG, "onFailure ${t.message}")
                    t.printStackTrace()
                    sendMessageBroadcast(t.message!!)
                    deleteFiles()
                    this@VideoUploadService.stopForeground(true)
                    stopSelf()
                }
            })
    }

    private fun sendMessageBroadcast(uploadMessage: String) {
        val intent = Intent("uploadingMessage")
        intent.putExtra("videoUploadMessage", uploadMessage)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun deleteFiles() {
        val folder = File(Constants.RECORD_PATH)
        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
            val filesInFolder =
                folder.listFiles()!! // This returns all the folders and files in your path
            for (file in filesInFolder) {
                file.delete()
            }
        }

        val compressedFolder = File(Constants.COMPRESSEDFILES_PATH)
        if (compressedFolder.listFiles() != null && compressedFolder.listFiles()!!.isNotEmpty()) {
            val filesInFolder =
                compressedFolder.listFiles()!! // This returns all the folders and files in your path
            for (file in filesInFolder) {
                file.delete()
            }
        }

        val mixFolder = File(Constants.MixAudioVideo_PATH)
        if (mixFolder.listFiles() != null && mixFolder.listFiles()!!.isNotEmpty()) {
            val filesInFolder =
                mixFolder.listFiles()!! // This returns all the folders and files in your path
            for (file in filesInFolder) {
                file.delete()
            }
        }

        val mergeFolder = File(Constants.MERGE_PATH)
        if (mergeFolder.listFiles() != null && mergeFolder.listFiles()!!.isNotEmpty()) {
            val filesInFolder =
                mergeFolder.listFiles()!! // This returns all the folders and files in your path
            for (file in filesInFolder) {
                file.delete()
            }
        }
        val gifFolder = File(Constants.GIF_Path)
        if (gifFolder.listFiles() != null && gifFolder.listFiles()!!.isNotEmpty()) {
            val filesInFolder =
                gifFolder.listFiles()!! // This returns all the folders and files in your path
            for (file in filesInFolder) {
                file.delete()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId, "SmyleeService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun updateNotificationText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(currentStateOfService)
                .setSmallIcon(R.drawable.ic_video_icon)
                .setContentIntent(pendingIntent)
                .build()
            manager.notify(1, notification)
        }
    }

    /*override fun onDestroy() {
        super.onDestroy()
    }*/

    override fun onSuccess(sucess: Boolean) {
        if (sucess) {
            val timeStamp: String =
                java.lang.String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            Logger.print("END TIME COMBINE**********************$timeStamp")

            val path: String = Constants.MERGE_PATH
            Log.d("Files", "Path: $path")
            val directory = File(path)
            val files = directory.listFiles()
            var nameFile: String? = ""

            if (files != null && files.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    nameFile = file.absolutePath
                }
            }

            val fileCompressed = File(nameFile!!)
            if (fileCompressed.length() <= 0) {
                fileCompressed.delete()
                deleteFiles()
                sendMessageBroadcast(resources.getString(R.string.video_files_empty))
//                Utils.showToastMessage(applicationContext,resources.getString(R.string.video_files_empty))
                this@VideoUploadService.stopForeground(true)
                stopSelf()
            } else {
                Log.d("FileName : Speed == ", "" + nameFile)
                currentStateOfService = resources.getString(R.string.compressing_video)
                updateNotificationText()
                if (idsSpeed.size == 0) {
                    if (isAudioMix) {
                        MixAudio(mixAudioDir!!, path, audioFileNameFinal, this).execute()
                    } else {
                        startCompressServiceForVideo(nameFile, mStopVideo)
                    }
                }
            }
        }
    }

    class MixAudio(
        private var mix_audio_dir: File,
        private var path_mix_video_audio: String,
        private var audio_file_name: String?,
        private val compressCallBack: CompressCallBack
    ) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            val folder = File(Constants.Audio_PATH)
            val filesInFolder: Array<File> =
                folder.listFiles()!! // This returns all the folders and files in your path
            Log.i("MainActivity", "Video files " + filesInFolder.size)
            Log.d("Files", "Path: $path_mix_video_audio")
            val directory = File(path_mix_video_audio)
            val files = directory.listFiles()
            var nameFile: String? = ""
            if (files != null && files.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    nameFile = file.absolutePath
                }
            }

            if (filesInFolder.isNotEmpty()) {
                val file = File(mix_audio_dir, "mixed_" + System.currentTimeMillis() + ".mp4")
                mux2(audio_file_name!!, nameFile!!, file.absolutePath)
            }
            return null
        }

        /*private fun mux(audioFile: String, videoFile: String, outFile: String) {
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFile)
            videoExtractor.selectTrack(0) // Assuming only one track per file. Adjust code if this is not the case.
            val videoFormat = videoExtractor.getTrackFormat(0)

            val audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(audioFile)
            audioExtractor.selectTrack(0) // Assuming only on   e track per file. Adjust code if this is not the case.
            val audioFormat = audioExtractor.getTrackFormat(0)

            // Init muxer
            val muxer = MediaMuxer(outFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val videoIndex = muxer.addTrack(videoFormat)
            val audioIndex = muxer.addTrack(audioFormat)
            muxer.start()

            // Prepare buffer for copying
            val maxChunkSize = 1024 * 1024
            val buffer = ByteBuffer.allocate(maxChunkSize)
            val bufferInfo = MediaCodec.BufferInfo()

            // Copy Video
            while (true) {
                val chunkSize = videoExtractor.readSampleData(buffer, 0)
                if (chunkSize > 0) {
                    bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.size = chunkSize
                    muxer.writeSampleData(videoIndex, buffer, bufferInfo)
                    videoExtractor.advance()
                } else {
                    break
                }
            }

            // Copy audio
            while (true) {
                val chunkSize = audioExtractor.readSampleData(buffer, 0)
                if (chunkSize >= 0) {
                    bufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    bufferInfo.flags = audioExtractor.sampleFlags
                    bufferInfo.size = chunkSize
                    muxer.writeSampleData(audioIndex, buffer, bufferInfo)
                    audioExtractor.advance()
                } else {
                    break
                }
            }

            // Cleanup
            muxer.stop()
            muxer.release()
            videoExtractor.release()
            audioExtractor.release()
        }*/

        private fun mux2(audioFile: String, videoFile: String, outFile: String) {
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFile)
            var videoFormat: MediaFormat? = null
            val videoTrackCount = videoExtractor.trackCount
            var frameMaxInputSize = 0
            var videoDuration = 0L
            var videoTrackIndex = -1
            var audioTrackIndex = -1

            for (i in 0 until videoTrackCount) {
                videoFormat = videoExtractor.getTrackFormat(i)
                val mimeType = videoFormat.getString(MediaFormat.KEY_MIME)!!
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i
                    frameMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION)
                    break
                }
            }
            if (videoTrackIndex < 0) {
                return
            }

            val audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(audioFile)
            var audioFormat: MediaFormat? = null
            val audioTrackCount = audioExtractor.trackCount
            for (i in 0 until audioTrackCount) {
                audioFormat = audioExtractor.getTrackFormat(i)
                val mimeType = audioFormat.getString(MediaFormat.KEY_MIME)!!
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0) {
                return
            }

            val videoBufferInfo = MediaCodec.BufferInfo()
            val mediaMuxer = MediaMuxer(outFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat!!)
            val writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat!!)

            try {
                mediaMuxer.start()

            } catch (e: IllegalStateException) {
                e.printStackTrace()

            }
            val byteBuffer = ByteBuffer.allocate(frameMaxInputSize)
            videoExtractor.unselectTrack(videoTrackIndex)
            videoExtractor.selectTrack(videoTrackIndex)

            while (true) {
                val readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0)
                if (readVideoSampleSize < 0) {
                    videoExtractor.unselectTrack(videoTrackIndex)
                    break
                }
                val videoSampleTime = videoExtractor.sampleTime
                videoBufferInfo.size = readVideoSampleSize
                videoBufferInfo.presentationTimeUs = videoSampleTime
                videoBufferInfo.offset = 0
                videoBufferInfo.flags = videoExtractor.sampleFlags
                try {
                    mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                videoExtractor.advance()
            }

            var audioPresentationTimeUs = 0L
            val audioBufferInfo = MediaCodec.BufferInfo()
            audioExtractor.selectTrack(audioTrackIndex)
            var lastEndAudioTimeUs = 0L
            while (true) {
                val readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0)
                if (readAudioSampleSize < 0) {
                    audioExtractor.unselectTrack(audioTrackIndex)
                    Log.i(
                        "MediaMuxer",
                        "AudioSampleSize $readAudioSampleSize PresentationTime $audioPresentationTimeUs"
                    )
                    if (audioPresentationTimeUs >= videoDuration) {
                        //if has reach the end of the video time ,just exit
                        break
                    } else {
                        //if not the end of the video time, just repeat.
                        lastEndAudioTimeUs += audioPresentationTimeUs
                        audioExtractor.selectTrack(audioTrackIndex)
                        continue
                    }
                }

                val audioSampleTime = audioExtractor.sampleTime
                audioBufferInfo.size = readAudioSampleSize
                audioBufferInfo.presentationTimeUs = audioSampleTime + lastEndAudioTimeUs
                Log.i(
                    "MediaMuxer",
                    "Presentation ${audioBufferInfo.presentationTimeUs} VideoDuration $videoDuration"
                )
                if (audioBufferInfo.presentationTimeUs > videoDuration) {
                    audioExtractor.unselectTrack(audioTrackIndex)
                    break
                }
                audioPresentationTimeUs = audioBufferInfo.presentationTimeUs
                audioBufferInfo.offset = 0
                audioBufferInfo.flags = audioExtractor.sampleFlags
                try {
                    mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                audioExtractor.advance()
            }

            Log.i("MediaMuxer", "Audio write success")
            try {
                mediaMuxer.stop()
                mediaMuxer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                videoExtractor.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                audioExtractor.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            compressOutPutFile()
        }

        private fun compressOutPutFile() {
            val path: String = Constants.MixAudioVideo_PATH
            Log.d("Files", "Path: $path")
            val directory = File(path)
            val files = directory.listFiles()
            var nameFile: String? = ""
            if (files != null && files.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    nameFile = file.absolutePath
                }
            }

            Log.d("VideoUploadService", "Audio Mixed File $nameFile")
            val file = File(nameFile!!)
            if (file.exists() && file.length() > 0) {
                compressCallBack.onVideoCompressed(nameFile)
            }
        }
    }

    /*private fun mux2(audioFile: String, videoFile: String, outFile: String, activity: Activity) {

        val videoExtractor = MediaExtractor()
        videoExtractor.setDataSource(videoFile)

        var videoFormat: MediaFormat? = null

        val videoTrackCount = videoExtractor.trackCount
        //vedio max input size
        var frameMaxInputSize = 0
        var frameRate = 0
        var videoDuration = 0L
        var videoTrackIndex = -1
        var audioTrackIndex = -1

        for (i in 0 until videoTrackCount) {
            videoFormat = videoExtractor.getTrackFormat(i)
            val mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("video/")) {
                videoTrackIndex = i
                frameMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                frameRate = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION);
                break
            }
        }

        if (videoTrackIndex < 0) {
            return
        }

        val audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioFile)
        var audioFormat: MediaFormat? = null

        val audioTrackCount = audioExtractor.trackCount

        for (i in 0 until audioTrackCount) {
            audioFormat = audioExtractor.getTrackFormat(i)

            val mimeType = audioFormat.getString(MediaFormat.KEY_MIME)!!
            if (mimeType.startsWith("audio/")) {
                audioTrackIndex = i
                break
            }
        }

        if (audioTrackIndex < 0) {
            return
        }

        val videoBufferInfo = MediaCodec.BufferInfo()
        val mediaMuxer = MediaMuxer(outFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat!!)
        val writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat!!)

        mediaMuxer.start()

        val byteBuffer = ByteBuffer.allocate(frameMaxInputSize)

        videoExtractor.unselectTrack(videoTrackIndex)
        videoExtractor.selectTrack(videoTrackIndex)

        while (true) {
            val readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0)
            if (readVideoSampleSize < 0) {
                videoExtractor.unselectTrack(videoTrackIndex)
                break
            }
            val videoSampleTime = videoExtractor.sampleTime
            videoBufferInfo.size = readVideoSampleSize
            videoBufferInfo.presentationTimeUs = videoSampleTime
            //videoBufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;
            videoBufferInfo.offset = 0
            videoBufferInfo.flags = videoExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
            videoExtractor.advance()
        }

        var audioPresentationTimeUs = 0L

        val audioBufferInfo = MediaCodec.BufferInfo()
        audioExtractor.selectTrack(audioTrackIndex)

        var lastEndAudioTimeUs = 0L

        while (true) {
            val readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0)
            if (readAudioSampleSize < 0) {
                audioExtractor.unselectTrack(audioTrackIndex);
                if (audioPresentationTimeUs >= videoDuration) {
                    //if has reach the end of the video time ,just exit
                    break
                } else {
                    //if not the end of the video time, just repeat.
                    lastEndAudioTimeUs += audioPresentationTimeUs;
                    audioExtractor.selectTrack(audioTrackIndex);
                    continue;
                }
            }

            val audioSampleTime = audioExtractor.sampleTime
            audioBufferInfo.size = readAudioSampleSize;
            audioBufferInfo.presentationTimeUs = audioSampleTime + lastEndAudioTimeUs;
            if (audioBufferInfo.presentationTimeUs > videoDuration) {
                audioExtractor.unselectTrack(audioTrackIndex)
                break;
            }
            audioPresentationTimeUs = audioBufferInfo.presentationTimeUs
            audioBufferInfo.offset = 0
            audioBufferInfo.flags = audioExtractor.sampleFlags
            mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo)
            audioExtractor.advance()
        }

        try {
            mediaMuxer.stop();
            mediaMuxer.release();
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            videoExtractor.release();
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            audioExtractor.release();
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //compressOutPutFile(true)
    }*/

    override fun onVideoCompressed(filePath: String) {
        val fileCompressed = File(filePath)
        if (fileCompressed.length() <= 0) {
            fileCompressed.delete()
            deleteFiles()
            sendMessageBroadcast(resources.getString(R.string.video_files_empty))
            this@VideoUploadService.stopForeground(true)
            stopSelf()
        } else {
            startCompressServiceForVideo(filePath, false)
        }
    }


    class TaskSaveGIF(
        var videopath: String,
        var durationFile: String,
        var width: Int,
        var height: Int,
        var videoDuration: Long,
        var thumbBitmap: Bitmap,
        var gifCompeleteListner: GifCompeleteListner
    ) : AsyncTask<Void?, Int?, String>() {
        //  var mediaMetadataRetriever: MediaMetadataRetriever? = null
        var maxDur: Long = 0
        val mediaMetadataRetriever = MediaMetadataRetriever()
        //  val mediaMetadataRetriever = FFmpegMediaMetadataRetriever()

        protected override fun doInBackground(vararg p0: Void?): String {

            try {
                //     maxDur = (1000 * durationFile.toDouble()).toLong()
                maxDur = (1000 * 2000.toDouble()).toLong()


            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

            Logger.print("width======$width")
            Logger.print("height======$height")

            try {

                mediaMetadataRetriever.setDataSource(videopath)
            } catch (e: java.lang.RuntimeException) {
                e.printStackTrace()
            }


            val gifPath = File(Constants.GIF_Path)
            if (!gifPath.exists()) {
                gifPath.mkdir()
            }
            val outFile = File(Constants.GIF_Path, Constants.GifName)
            return try {
                val bos =
                    BufferedOutputStream(FileOutputStream(outFile))
                bos.write(genGIF(width, height, durationFile))
                bos.flush()
                bos.close()
                outFile.absolutePath
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                e.message
            } catch (e: IOException) {
                e.printStackTrace()
                e.message
            }!!
        }


        override fun onPostExecute(result: String) {

            Logger.print("GIF GENERATED ====$result")
            gifCompeleteListner.onComplete(true, thumbBitmap, videopath, videoDuration)


        }

        protected override fun onProgressUpdate(vararg values: Int?) {
            //bar.progress = values[0]!!
            //  updateFrame()
        }

        /*private fun genGIF(width: Int, height: Int, durationFile: String): ByteArray {
            val bos = ByteArrayOutputStream()
            Logger.print("durationFile================${maxDur}")
            val animatedGifEncoder = AnimatedGifEncoder()
            animatedGifEncoder.setDelay(300)

            var bmFrame: Bitmap
            animatedGifEncoder.start(bos)

            run {
                var i = 0
                while (i <= 2000) {
                    val frameTime = (i + 100).toLong()
                    Logger.print("frameTime else =============$frameTime")
                    Logger.print("frameTime i value  =============$i")
                    try {
                        bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime*1000, OPTION_CLOSEST)
                        animatedGifEncoder.addFrame(bmFrame)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    i += 100
                }
            }
            animatedGifEncoder.finish()
            return bos.toByteArray()
        }
*/
        private fun genGIF(width: Int, height: Int, durationFile: String): ByteArray {
            val bos = ByteArrayOutputStream()
            Logger.print("durationFile================${maxDur.toLong()}")
            val animatedGifEncoder = AnimatedGifEncoder()
            // animatedGifEncoder.setDelay(1000)
            animatedGifEncoder.setDelay(300)

            var bmFrame: Bitmap
            animatedGifEncoder.start(bos)
            val numThumbs = 9
            val interval: Long = maxDur / numThumbs
            Logger.print("numThumbs==========$numThumbs")
            Logger.print("interval==========$interval")
            run {
                var i = 0
                while (i <= 100) {
                    // while (i <= numThumbs) {
                    val frameTime = maxDur * i / 100
                    // val frameTime = i *interval
                    Logger.print("frameTime else =============$frameTime")
                    Logger.print("frameTime i value  =============$i")

                    try {
                        bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime, OPTION_CLOSEST)
                        animatedGifEncoder.addFrame(bmFrame)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //publishProgress(i)
                    i += 20
                }
            }


            //last from at end
//            bmFrame = mediaMetadataRetriever.getFrameAtTime(maxDur)
//            animatedGifEncoder.addFrame(bmFrame)
            animatedGifEncoder.finish()

            return bos.toByteArray()
        }

        init {
            /*Toast.makeText(
                this@MainActivity,
                "Generate GIF animation",
                Toast.LENGTH_LONG
            ).show()*/
        }
    }

    class TaskSaveGIFFfmpeg(
        private var videopath: String,
        private var durationFile: String,
        var width: Int,
        var height: Int,
        private var videoDuration: Long,
        private var thumbBitmap: Bitmap,
        private var gifCompeleteListner: GifCompeleteListner
    ) : AsyncTask<Void?, Int?, String>() {
        private var maxDur: Long = 0

        override fun doInBackground(vararg p0: Void?): String {
            try {
                maxDur = (1000 * durationFile.toDouble()).toLong()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

            Logger.print("width======$width")
            Logger.print("height======$height")

            val gifPath = File(Constants.GIF_Path)
            if (!gifPath.exists()) {
                gifPath.mkdir()
            }
            val outFile = File(Constants.GIF_Path, Constants.GifName)
            if (outFile.exists()) {
                outFile.delete()
            }
            outFile.createNewFile()

            genGifFfmpeg(outFile.absolutePath)
            return outFile.absolutePath
        }

        override fun onPostExecute(result: String) {
            Logger.print("GIF GENERATED ====$result")
            gifCompeleteListner.onComplete(true, thumbBitmap, videopath, videoDuration)
        }

        override fun onProgressUpdate(vararg values: Int?) {}

        private fun genGifFfmpeg(gifFilePath: String) {
            Logger.print("gifFilePath ====$gifFilePath")
            Logger.print("videopath =========$videopath")
            // val command = "-i $videopath -r 15 -vf scale=256:-1 -ss 00:00:01 -to 00:00:03 $gifFilePath -y"
            val command =
                "-i $videopath -r 10 -vf scale=384:-1 -ss 00:00:01 -to 00:00:03 $gifFilePath -y"


            FFmpeg.execute(command)
            Logger.print("command =========$command")

        }
    }

    override fun onComplete(
        sucess: Boolean,
        bitmap: Bitmap,
        video_file: String?,
        videoDuration: Long
    ) {
        persistImage(
            selectedCatIds,
            selectedLanguageIds,
            bitmap,
            video_file,
            postTitle,
            videoDuration
        )
    }


}