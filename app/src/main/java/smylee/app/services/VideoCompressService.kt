package smylee.app.services

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import smylee.app.engine.VideoResolutionChanger
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class VideoCompressService : IntentService("VideoCompressService") {
    private var filePathForCompress: String = ""
    private var mStopVideoORNOT: Boolean? = false
    private var filePath: File? = null
    var TAG = "IntentServiceForVideoCompress"

    /*override fun onHandleIntent(intent: Intent) {
        Log.d("call onHandleIntent", "onHandleIntent")
        if (intent != null) {
            val bundle = intent.extras
            if (bundle != null) {
                filePathForCompress = bundle.getString("filePath")
                mStopVideoORNOT = bundle.getBoolean("mStopVideo")
                Log.d("filePathForCompress", filePathForCompress)
                filePath = File(filePathForCompress)
                startCompressVideoProcess(filePath!!, mStopVideoORNOT!!)
            } else {
                Log.d("BUNDLE", "BUNDLE IS NULL")
            }
        } else {
            Log.d("INTENT", "INTENT IS NULL")
        }
    }*/

    private fun sendCompletionBroadCast() {
        val intent = Intent("doneTristateCompressBroadcast")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun startCompressVideoProcess(filePAth: File, mStopVideoORNOT: Boolean) {
        try {
            val pathToReEncodedFile = VideoResolutionChanger(baseContext).changeResolution(
                filePAth,
                mStopVideoORNOT,
                false
            )
            val compressedFilePath = File(pathToReEncodedFile)
            Log.d("compressedFilePath", compressedFilePath.absolutePath + "")
            //final File compressedFilePath = new File(pathToReEncodedFile);
            Handler(Looper.getMainLooper()).post {
                Log.d("Compress Complete", compressedFilePath.absolutePath)
                Log.d("Compress File SIZE", fileSize(compressedFilePath.absoluteFile))
                sendCompletionBroadCast()
            }
        } catch (ex: Exception) {
            sendCompletionBroadCast()
            showMessage(ex.message)
            if (mStopVideoORNOT) {
                println("call or not catch mStopVideoORNOT!!!!!!!!!!!!!!$mStopVideoORNOT")
            }
            Log.d(TAG, ex.message, ex)
        } catch (throwable: Throwable) {
            sendCompletionBroadCast()
            showMessage(throwable.message)
            if (mStopVideoORNOT) {
                println("call or not throwable mStopVideoORNOT!!!!!!!!!!!!!!$mStopVideoORNOT")
            }

            Log.d(TAG, throwable.message, throwable)
        }
    }

    private fun fileSize(file: File): String {
        return readableFileSize(file.length())
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "$size B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return (DecimalFormat("#,##0.##").format(size / 1024.0.pow(digitGroups.toDouble()))
                + " " + units[digitGroups])
    }

    private fun showMessage(errorMessage: String?) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onHandleIntent(p0: Intent?) {
        Log.d("call onHandleIntent", "onHandleIntent")
        if (p0 != null) {
            val bundle = p0.extras
            if (bundle != null) {
                filePathForCompress = bundle.getString("filePath")!!
                mStopVideoORNOT = bundle.getBoolean("mStopVideo")
                Log.d("filePathForCompress", filePathForCompress)
                filePath = File(filePathForCompress)
                startCompressVideoProcess(filePath!!, mStopVideoORNOT!!)
            } else {
                Log.d("BUNDLE", "BUNDLE IS NULL")
            }
        } else {
            Log.d("INTENT", "INTENT IS NULL")
        }
    }
}