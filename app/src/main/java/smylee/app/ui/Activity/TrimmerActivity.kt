package smylee.app.ui.Activity

import smylee.app.ui.base.BaseActivity
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.video_trim.K4LVideoTrimmer
import com.video_trim.interfaces.OnK4LVideoListener
import com.video_trim.interfaces.OnTrimVideoListener
import smylee.app.R
import smylee.app.utils.Logger


class TrimmerActivity : BaseActivity(), OnTrimVideoListener, OnK4LVideoListener {

    private var mVideoTrimmer: K4LVideoTrimmer? = null
    private var mProgressDialog: ProgressDialog? = null
    private var trimSec: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        val extraIntent: Intent? = intent
        var path = ""

        if (extraIntent != null) {
            path = extraIntent.getStringExtra(CameraVideoRecording.EXTRA_VIDEO_PATH)!!
            trimSec = extraIntent.getIntExtra("trimSec", 0)
            Logger.print("path!!!!!!!!!!!!!!!!!!!$path")
        }

        //setting progressbar
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setCancelable(false)
        mProgressDialog?.setMessage(getString(R.string.trimming_your_video))

        mVideoTrimmer = findViewById<View>(R.id.timeLine) as K4LVideoTrimmer
        if (mVideoTrimmer != null) {
            println("trimSec!!!!!!!!!!!!$trimSec")
            mVideoTrimmer?.setMaxDuration(trimSec)
            mVideoTrimmer?.setOnTrimVideoListener(this)
            mVideoTrimmer?.setOnK4LVideoListener(this)
            mVideoTrimmer?.setVideoURI(Uri.parse(path))
            mVideoTrimmer?.setVideoInformationVisibility(true)
        }
    }

    override fun onTrimStarted() {
        mProgressDialog?.show()
    }

    override fun getResult(uri: Uri?) {
        mProgressDialog!!.cancel()

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onError(message: String?) {
        mProgressDialog!!.cancel()

        runOnUiThread {
            Toast.makeText(this@TrimmerActivity, message, Toast.LENGTH_SHORT).show()
            cancelAction()
        }
    }

    override fun onBackPressed() {
        cancelAction()
        super.onBackPressed()
    }

    override fun cancelAction() {
        mProgressDialog?.cancel()
        mVideoTrimmer?.destroy()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onVideoPrepared() {
        runOnUiThread {
            //  Toast.makeText(this@TrimmerActivity, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }
    }
}