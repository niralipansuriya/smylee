package smylee.app.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
import smylee.app.CallBacks.ExtractAudioCallbacks
import smylee.app.CallBacks.ExtractListner
import smylee.app.CallBacks.MergerCallBacks
import smylee.app.R
import smylee.app.custom.AudioExtractor
import smylee.app.listener.OnShareVideoPrepareListener
import java.io.*
import java.net.URL

class ShareVideoUtils {

    companion object : ExtractAudioCallbacks, MergerCallBacks {

        private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent) {
                val referenceId: Long = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                list.remove(referenceId)
                if (list.isEmpty()) {
                    Log.e("INSIDE", "" + referenceId)
                }
                prepareVideo(newFilePath, contextOfActivity!!)
                ctxt!!.unregisterReceiver(this)
            }
        }

        val list: ArrayList<Long> = ArrayList()
        private var isVideoPreparing: Boolean = false
        private var contextOfActivity: Context? = null
        private var onShareVideoPrepareListener: OnShareVideoPrepareListener? = null
        private var onVideoExtractListener: ExtractListner? = null
        private var videoHeight: Int = 0
        private var videoWidth: Int = 0
        private var isExtractVideo: Boolean = false
        private var ISDownLoadVideo: Boolean = false
//        private val mCombineListener: VideoCombiner.VideoCombineListener? = null

        private var newFilePath = ""

        fun shareVideoFile(context: Context, videoLink: String, videoWidth: Int, videoHeight: Int,
            onShareVideoPrepareListener: OnShareVideoPrepareListener) {

            this.onShareVideoPrepareListener = onShareVideoPrepareListener
            contextOfActivity = context
            this.videoHeight = videoHeight
            this.videoWidth = videoWidth
            this.isExtractVideo = false
            this.ISDownLoadVideo = true

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(videoLink)

            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("Smylee Video")
            request.setDescription("Downloading smylee video")

            newFilePath = "Smylee_" + System.currentTimeMillis() + ".mp4"
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, newFilePath)
            newFilePath = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newFilePath
            val refid = downloadManager.enqueue(request)
            list.add(refid)
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            /*val httpProxyCacheServer = BaseActivity.getProxy(context)
            if (httpProxyCacheServer!!.isCached(videoLink)) {
                Logger.print("httpProxyCacheServer !!!!!!!!!!!!!!!!" + httpProxyCacheServer.isCached(videoLink))
                val cacheFileUri = httpProxyCacheServer.getProxyUrl(videoLink)
                Logger.print("ForYouFragmentHome > CacheFilePath $cacheFileUri")
                if (!isVideoPreparing) {
                    isVideoPreparing = true
                    prepareVideo(cacheFileUri, context)
                }
            } else {
                Logger.print("registerCacheListener !!!!!!!!!!!!!!!!")
                httpProxyCacheServer.registerCacheListener(cacheListener, videoLink)
            }*/
        }

        fun extractVideoFile(context: Context, videoLink: String, extractListener: ExtractListner) {
            this.onVideoExtractListener = extractListener
            contextOfActivity = context
            this.isExtractVideo = true
            this.ISDownLoadVideo = false

            val rootDirectory = File(Constants.EXTRACT_PATH)
            val audioPath = File(Constants.Audio_PATH)
            if (!audioPath.exists()) {
                audioPath.mkdir()
            }

            Methods.deleteFilesFromDirectory(Constants.Audio_PATH)
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs()
                DownloadFileFromURL(this).execute(videoLink)
            } else {
                DownloadFileFromURL(this).execute(videoLink)
            }
        }

        private fun prepareVideo(filePath: String, context: Context) {
            Logger.print("prepareVideo !!!!!!!!!!!!!!!!!!!!!!!")
            val rootDirectory = File(Constants.ROOT_PATH)
            val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
            //  val lastGifFile = File(Constants.ROOT_PATH, "last_slide.mp4")
            val lastGifFile = File(Constants.ROOT_PATH, "last_slide.mp4")
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs()
                gifFile.createNewFile()
                installBinaryFromRaw(context, R.raw.smylee_gif_new, gifFile)
            } else if (!gifFile.exists()) {
                gifFile.createNewFile()
                installBinaryFromRaw(context, R.raw.smylee_gif_new, gifFile)
            }
            if (!lastGifFile.exists()) {
                lastGifFile.createNewFile()
                installBinaryFromRaw(context, R.raw.last_slide_video, lastGifFile)
            }

            val fileDirectory = File(Constants.DOWNLOADED_PATH)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            val newFilePath = Constants.DOWNLOADED_PATH + "Smylee_" + System.currentTimeMillis() + ".mp4"
            val tmpFilePath = Constants.DOWNLOADED_PATH + "Smylee_" + System.currentTimeMillis() + "1.mp4"
            Logger.print("ForYouFragmentHome > New File Path $newFilePath")
            val newVideoFile = File(newFilePath)
            val tmpVideoFile = File(tmpFilePath)
            newVideoFile.createNewFile()
            tmpVideoFile.createNewFile()

            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void?): Void? {
                    val command: String
                    val standardWidth = 360.0f
                    val standardHeight = 640.0f
                    if (videoHeight > 100 && videoWidth > 100) {
                        val scaleWidth = (videoWidth / standardWidth) * 100
                        val scaleHeight = (videoHeight / standardHeight) * 100
                        Logger.print("ShareVideoUtils > Scale Width $scaleWidth")
                        Logger.print("ShareVideoUtils > Scale Height $scaleHeight")
                        var scale: Int
                        if (videoWidth > videoHeight) {
                            if (scaleHeight > scaleWidth) {
                                scale = scaleWidth.toInt()
                            } else {
                                scale = scaleHeight.toInt()
                            }
                            if (scale < 75) {
                                scale = 75
                            }
                        } else {
                            if (scaleHeight > scaleWidth) {
                                scale = scaleHeight.toInt()
                            } else {
                                scale = scaleWidth.toInt()
                            }
                        }
                        Logger.print("ShareVideoUtils > Scale $scale")
                        command =
                            "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scale:$scale[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scale:$scale[c];[tmp][c] overlay=${videoWidth - (100 + (scale - 100))}:${videoHeight - (100 + (scale - 100))}:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
                    } else if (videoHeight > 100) {
                        val scaleHeight = (videoHeight / standardHeight) * 100
                        Logger.print("ShareVideoUtils > Scale Height $scaleHeight")
                        command =
                            "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scaleHeight:$scaleHeight[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scaleHeight:$scaleHeight[c];[tmp][c] overlay=10:${videoHeight - (100 + (scaleHeight - 100))}:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
                    } else if (videoWidth > 100) {
                        val scaleWidth = (videoWidth / standardWidth) * 100
                        Logger.print("ShareVideoUtils > Scale Width $scaleWidth")
                        command =
                            "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scaleWidth:$scaleWidth[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scaleWidth:$scaleWidth[c];[tmp][c] overlay=${videoWidth - (100 + (scaleWidth - 100))}:10:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
                    } else {
                        command =
                            "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[0:v][1:v] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [tmp][2:v] overlay=10:10:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
                    }
                    FFmpeg.execute(command)

                    /*val filepath = File(Constants.DOWNLOADED_PATH,"list.txt") // file path to save
                    val writer = FileWriter(filepath)
                    writer.append("file '$tmpFilePath'\nfile '${lastGifFile.absolutePath}'")
                    writer.flush()
                    writer.close()

                    val concatCommand = "-f concat -safe 0 -i ${filepath.absolutePath} -c copy $newFilePath"
                    val concatCommand = "-f concat -safe 0 -i ${filepath.absolutePath} -c:v copy -c:a copy $newFilePath"
                    val concatCommand = "-i concat:$tmpFilePath|${lastGifFile.absolutePath} -c copy $newFilePath"
                    val concatCommand = "-i $tmpFilePath -i ${lastGifFile.absolutePath} -filter_complex \"[0:v]pad=iw*2:ih[int]; [int][1:v]overlay=W/2:0[vid]\" -map \"[vid]\" $newFilePath"
                    FFmpeg.execute(concatCommand)
                    tmpVideoFile.delete()
                    filepath.delete()*/
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    File(filePath).delete()
                    scanDownloadedFile(context)
                    isVideoPreparing = false
                    Logger.print("FFmpeg > onSuccess")
                    onShareVideoPrepareListener!!.onVideoReadyToShare(newFilePath)

                }

            }.execute()
        }

        private fun scanDownloadedFile(context: Context) {
            MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStorageDirectory().toString()),null) { path, uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        }

        private fun installBinaryFromRaw(context: Context, resId: Int, file: File?) {
            val rawStream: InputStream = context.resources.openRawResource(resId)
            val binStream: OutputStream = getFileOutputStream(file)!!
            pipeStreams(rawStream, binStream)
            try {
                rawStream.close()
                binStream.close()
            } catch (e: IOException) {
                Log.e("MainActivity", "Failed to close streams!", e)
            }
        }

        private fun getFileOutputStream(file: File?): OutputStream? {
            try {
                return FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                Log.e("MainActivity", "File not found attempting to stream file.", e)
            }
            return null
        }

        private fun pipeStreams(`is`: InputStream, os: OutputStream) {
            val buffer = ByteArray(1024)
            var count: Int
            try {
                while (`is`.read(buffer).also { count = it } > 0) {
                    os.write(buffer, 0, count)
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Error writing stream.", e)
            }
        }

        override fun OnExtractFinish() {
            onVideoExtractListener?.onExtractFinished()
            Logger.print("ShareVideo Utils ************************************888")
        }

        override fun onExtractFail(errorMassage: String) {
            Utils.showAlert(contextOfActivity!!, "",errorMassage)
        }

        private class DownloadFileFromURL internal constructor(var context1: Companion) : AsyncTask<String?, String?, String?>() {
            var nameoffile: String? = null

            private fun back() {
                val file = File(Constants.Audio_PATH,"extract_" + System.currentTimeMillis() + ".m4a")
                val directory = File(Constants.EXTRACT_PATH)
                val files = directory.listFiles()
                var videoFile: String? = ""
                if (files != null && files.isNotEmpty()) {
                    for (fileOfDir in directory.listFiles()!!) {
                        videoFile = fileOfDir.absolutePath
                    }
                }

                Logger.print("videoFile !!!!!!!!!!!!!!!!!!!!!!!!!!$videoFile")
                val audioExtractor = AudioExtractor()
                audioExtractor.setCallback(context1)
                try {
                    audioExtractor.genVideoUsingMuxer(videoFile,file.absolutePath,-1,-1,true,false)
                } catch (e: Exception) {
                    context1.onExtractFail("Fail to extract audio from video! please try again later")
                }
            }

            override fun onProgressUpdate(vararg values: String?) {
            }

            override fun onPostExecute(file_url: String?) {
                if (file_url == "Success") {
                    back()
                }
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
                    output = FileOutputStream(Constants.EXTRACT_PATH + "" + nameoffile)

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

        override fun onSuccess(sucess: Boolean) {
            Logger.print("combine dinisheddddddddd")
        }
    }
}