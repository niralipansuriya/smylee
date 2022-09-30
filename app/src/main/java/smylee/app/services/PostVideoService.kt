/*
package smylee.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.webkit.MimeTypeMap
import com.arthenica.mobileffmpeg.FFmpeg
import com.facebook.AccessToken
import com.facebook.GraphRequest
import smylee.app.R
import smylee.app.home.HomeActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.Methods
import smylee.app.utils.ShareVideoUtils
import java.io.*


class PostVideoService : Service() {
    public var videoHeight: Int = 0
    public var videoWidth: Int = 0
    private var currentStateOfService = "Preparing Upload........."
    private lateinit var pendingIntent: PendingIntent

    private var isFbPost: Boolean = false
    private var isInstaPost: Boolean = false
    var inputFile: String = ""
    var oputPutFile: String = ""
    var inputPath: String = ""
    var outputPath: String = ""
    var postTitle: String = ""
    var finalVideoFile: String = ""
    var finalVideoFileName: String = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, HomeActivity::class.java)
        notificationIntent.putExtra("screenName", "")

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
        if (intent != null) {
            postTitle = intent.getStringExtra("postTitle")!!
            finalVideoFile = intent.getStringExtra("finalVideoFile")!!
            finalVideoFileName = intent.getStringExtra("finalVideoFileName")!!
            videoWidth = intent.getIntExtra("videoWidth", 0)
            videoHeight = intent.getIntExtra("videoHeight", 0)
            isFbPost = intent.getBooleanExtra("isFbPost", false)
            isInstaPost = intent.getBooleanExtra("isInstaPost", false)


            inputPath = finalVideoFile
            inputFile = ""
            oputPutFile = finalVideoFileName
            //outputPath = Constants.DOWNLOADED_PATH
            outputPath = Constants.Share_Path
            Logger.print("finalVideoFile!!!!!!!!!!!!$finalVideoFile")
            val sharePostPath = File(Constants.Share_Path)
            if (!sharePostPath.exists()) {
                sharePostPath.mkdir()
            }

            //   val f = File(Constants.DOWNLOADED_PATH + finalVideoFileName)
            val f = File(Constants.Share_Path + finalVideoFileName)
            if (f.exists()) {
                f.delete()
            }
            f.createNewFile()

            copyFileAsnc(
                inputPath,
                inputFile,
                outputPath,
                oputPutFile,
                videoHeight,
                videoWidth,
                this
            ).execute()
        } else {
            Logger.print("intent null ==========")
        }



        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
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
            val notification: Notification = Notification.Builder(
                this,
                channelId
            )
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(currentStateOfService)
                .setSmallIcon(R.drawable.ic_video_icon)
                .setContentIntent(pendingIntent)
                .build()
            manager.notify(1, notification)
        }
    }

    class copyFileAsnc(
        var inputPath: String,
        var inputFile: String,
        var outputPath: String,
        var oputPutFile: String, var videoHeight: Int, var videoWidth: Int, var ctx: Context
    ) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg p0: Void?): Void? {
            copyFile(inputPath, inputFile, outputPath, oputPutFile, videoHeight, videoWidth, ctx)
            return null
        }

        private fun copyFile(
            inputPath: String,
            inputFile: String,
            outputPath: String,
            oputPutFile: String,
            videoHeight: Int,
            videoWidth: Int,
            context: Context
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
            val rootDirectory = File(Constants.ROOT_PATH)

            val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs()
                gifFile.createNewFile()
                installBinaryFromRaw(ctx, R.raw.smylee_gif_new, gifFile)
            } else if (!gifFile.exists()) {
                gifFile.createNewFile()
                installBinaryFromRaw(ctx, R.raw.smylee_gif_new, gifFile)
            }
            //  val fileDirectory = File(Constants.DOWNLOADED_PATH)
            val fileDirectory = File(Constants.Share_Path)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
   val newFilePath = Constants.DOWNLOADED_PATH + "Smylee_" + System.currentTimeMillis() + ".mp4"
               val tmpFilePath = Constants.DOWNLOADED_PATH + "Smylee_" + System.currentTimeMillis() + "1.mp4"

            val newFilePath = Constants.Share_Path + "Smylee_" + System.currentTimeMillis() + ".mp4"
            val tmpFilePath =
                Constants.Share_Path + "Smylee_" + System.currentTimeMillis() + "1.mp4"
            Logger.print("ForYouFragmentHome > New File Path $newFilePath")
            val newVideoFile = File(newFilePath)
            val tmpVideoFile = File(tmpFilePath)
            //  var FILEPATH = Methods.filePathFromDir(Constants.DOWNLOADED_PATH)
            var FILEPATH = Methods.filePathFromDir(Constants.Share_Path)
            Logger.print("final file path =====================${Methods.filePathFromDir(Constants.Share_Path)}")
            AddGIF(FILEPATH!!, videoWidth, videoHeight, tmpFilePath).execute()
            Logger.print("copyFile onPostExecute======${result.toString()}")
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
    }

    class AddGIF(
        var filePath: String,
        var videoWidth: Int,
        var videoHeight: Int,
        var tmpFilePath: String
    ) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")

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

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            File(filePath).delete()
            // scanDownloadedFile(context)
            Logger.print("FFmpeg > onSuccess onPostExecute")

            Logger.print("path of post video ==============${Methods.filePathFromDir(Constants.Share_Path)}")
            //shareVideoFB(Methods.filePathFromDir(Constants.DOWNLOADED_PATH)!!)
            shareVideoFB(Methods.filePathFromDir(Constants.Share_Path)!!)
        }

        var data: ByteArray? = null

        fun shareVideoFB(videoPath: String) {
            val accessToken = AccessToken.getCurrentAccessToken()
            Logger.print("accessToken===============$accessToken")
            val fileUri: Uri = Uri.parse(videoPath)
            val fileName =
                videoPath.substring(videoPath.lastIndexOf('/') + 1, videoPath.length)
            //  val mimeType: String = this.getMimeType(videoPath)!!
            try {
                data = readBytes(videoPath)!!
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val videoSize: Int = data!!.size
            val privacy: String = this.getPrivacy("Friends Only")!!
            val request = GraphRequest.newPostRequest(
                accessToken,
                "/me/videos",
                null
            ) { response ->
                try {
                    // if (pd.isShowing) pd.dismiss()
                } catch (e: Exception) {
                }
                Logger.print("response ====== ${response.toString()}")
                Logger.print("error response ====== ${response.error}")
                // onFBShareVideoCompleted(response)
            }
            var params = request.parameters
            params = request.parameters
            params.putByteArray(fileName, data)
            params.putString("title", "This is video you can check")
            params.putString("description", "Some Description...")

//        params.putString("upload_phase", "start");
            params.putInt("file_size", data!!.size)
            request.parameters = params
            request.executeAsync()
        }

        @Throws(IOException::class)
        fun readBytes(dataPath: String?): ByteArray? {
            val inputStream: InputStream = FileInputStream(dataPath)
            val byteBuffer = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int = 0
            while (inputStream.read(buffer).also({ len = it }) != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        }

        private fun getPrivacy(privacy: String): String? {
            var str: String
            if (privacy.equals("Everyone", ignoreCase = true)) str = "EVERYONE"
            str = if (privacy.equals(
                    "Friends and Networks",
                    ignoreCase = true
                )
            ) "NETWORKS_FRIENDS" else if (privacy.equals(
                    "Friends of Friends",
                    ignoreCase = true
                )
            ) "FRIENDS_OF_FRIENDS" else if (privacy.equals(
                    "Friends Only",
                    ignoreCase = true
                )
            ) "ALL_FRIENDS" else if (privacy.equals(
                    "Custom",
                    ignoreCase = true
                )
            ) "CUSTOM" else if (privacy.equals(
                    "Specific People...",
                    ignoreCase = true
                )
            ) "SOME_FRIENDS" else "SELF"
            return str
        }

    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }


    private fun scanDownloadedFile(context: Context) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(Environment.getExternalStorageDirectory().toString()),
            null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    companion object {
        private const val channelId = "PostVideoService"
        private const val TAG = "PostVideoService"
    }

}
*/
