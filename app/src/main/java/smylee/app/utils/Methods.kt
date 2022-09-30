package smylee.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.activity_for_you_fragment.*
import smylee.app.BuildConfig
import smylee.app.R
import java.io.File
import java.text.DecimalFormat
import kotlin.math.abs

class Methods {
    companion object {
        /*fun setStatusBarGradiant(activity: Activity, drawable: Int) {
            val window = activity.window
            val background = ContextCompat.getDrawable(activity, drawable)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
            window.setBackgroundDrawable(background)
        }*/

        fun openApplicationSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.data = uri
            activity.startActivityForResult(intent, 88)
        }

        /*fun getDate(milliSeconds: Long, dateFormat: String?): String? {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(dateFormat)

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(milliSeconds)
            return formatter.format(calendar.getTime())
        }*/

        fun deleteFilesFromDirectory(dirPath: String) {
            val dir = File(dirPath)
            val files = dir.listFiles()

            if (files != null && files.isNotEmpty()) {
                for (file in dir.listFiles()!!) {
                    file.delete()
                }
            }
        }

        fun setImageRotation(imgExtractAudio: AppCompatImageView) {
            val rotate = RotateAnimation(
                0F,
                360F,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 5000
            rotate.repeatCount = Animation.INFINITE
            rotate.repeatMode = Animation.RESTART
            rotate.interpolator = LinearInterpolator()
            imgExtractAudio.startAnimation(rotate)
        }

        fun deleteFileWithZeroKB(dirPath: String) {
            val folder = File(dirPath)
            val filesInFolder = folder.listFiles()!!
            if (filesInFolder[0].length() <= 0) {
                filesInFolder[0].delete()
            }
        }

        fun showFollowerCounts(
            actual_view_counts: String,
            tv_follower_counts: AppCompatTextView,
            context: Context?
        ) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            tv_follower_counts.text =
                numberString1 + " " + context?.resources?.getString(R.string.followers)
        }

        fun showLikeCounts(actual_view_counts: String, like_txt: AppCompatTextView) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            like_txt.text = numberString1
        }

        fun showCommentLikeCounts(actual_view_counts: String, like_txt: TextView) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            like_txt.text = numberString1
        }

        fun showCommentCounts(actual_view_counts: String, comment_txt: AppCompatTextView) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            comment_txt.text = numberString1
        }

        fun showCommentCountsDialog(
            actual_view_counts: String,
            comment_txt: AppCompatTextView?,
            context: Context?
        ) {

            println("showCommentCountsDialog counts!!!!!!!!!$actual_view_counts")
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }

            if (numberString1.contentEquals("0") || numberString1.contentEquals("1")) {
                comment_txt!!.text = "$numberString1  ${context?.resources?.getString(R.string.comment)}"
            } else {
                comment_txt!!.text =
                    numberString1 + " " + context?.resources?.getString(R.string.comments)
            }

        }

        fun showViewCounts(actual_view_counts: String, tvViewCounts: AppCompatTextView) {
            val number1: String = actual_view_counts
            val numberString1: String
            val number2: String
            val df = DecimalFormat("###.#")

            when {
                abs(number1.toLong() / 1000000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "B"
                }
                abs(number1.toLong() / 1000000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "M"
                }
                abs(number1.toLong() / 1000) >= 1 -> {
                    number2 = (number1.toDouble() / 1000).toString()
                    val val1 = number2.toDouble()
                    val val2 = df.format(val1)
                    numberString1 = val2 + "K"
                }
                else -> {
                    numberString1 = number1
                }
            }
            tvViewCounts.text = numberString1
        }

        fun fileFromDir(dirPath: String): String? {
            var fileName = ""
            val directory = File(dirPath)
            val files = directory.listFiles()
//            var nameFile: String? = ""
            if (files != null && files.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    fileName = file.name
                }
            }
            return fileName
        }

        fun filePathFromDir(dirPath: String): String? {
            var fileName = ""
            val directory = File(dirPath)
            val files = directory.listFiles()
//            var nameFile: String? = ""
            if (files != null && files.isNotEmpty()) {
                for (file in directory.listFiles()!!) {
                    fileName = file.absolutePath
                }
            }
            return fileName
        }

        fun animateHeart(view: ImageView) {
            val scaleAnimation = ScaleAnimation(
                0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            prepareAnimation(scaleAnimation)
            val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
            prepareAnimation(alphaAnimation)
            val animation = AnimationSet(true)
            animation.addAnimation(alphaAnimation)
            animation.addAnimation(scaleAnimation)
            animation.duration = 700
            animation.fillAfter = true
            view.startAnimation(animation)
        }

        private fun prepareAnimation(animation: Animation): Animation? {
            animation.repeatCount = 1
            animation.repeatMode = Animation.REVERSE
            return animation
        }
    }
}