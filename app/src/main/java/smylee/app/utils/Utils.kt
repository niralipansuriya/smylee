package smylee.app.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import smylee.app.dialog.CommonDialog
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.nio.charset.Charset
import java.util.regex.Pattern

object Utils {

    const val TAG = "Utils"
    /*val AUDIO_FORMAT: String? = ".m4a"
    const val AUDIO_MIME_TYPE = "audio/m4a"*/

    fun deleteProfileDataFile(context: Context) {
        val folderObject = File(context.externalCacheDir, "profileData.JSON")
        if (folderObject.exists()) {
            folderObject.delete()
        }
    }

    fun writeProfileDataToFile(context: Context, profileData: String) {
        Logger.print("profileData==================$profileData")
        val folderObject = File(context.externalCacheDir, "profileData.JSON")
        val fileOutputStream = FileOutputStream(folderObject)
        val fileWriter = OutputStreamWriter(fileOutputStream)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(profileData)
        bufferedWriter.close()
    }

    fun readProfileDataFromFile(context: Context): String {
        val folderObject = File(context.externalCacheDir, "profileData.JSON")
        return if (folderObject.exists()) {
            val fileInputStream = FileInputStream(folderObject)
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()
            while (fileInputStream.read(buffer) != -1) {
                val stringData = String(buffer, Charset.forName("UTF-8"))
                stringBuilder.append(stringData.trim())
            }
            fileInputStream.close()
            stringBuilder.toString()
        } else {
            ""
        }
    }

    fun hideKeyboard(ctx: Context) {
        val inputManager = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (ctx as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun hideKeyboardActivity(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @Throws(IOException::class)
    private fun cleanDirectory(file: File) {
        if (!file.exists()) {
            return
        }
        val contentFiles = file.listFiles()
        if (contentFiles != null) {
            for (contentFile in contentFiles) {
                delete(contentFile)
            }
        }
    }

    //validations
    fun isTextEmpty(appCompatEditText: AppCompatEditText): Boolean {
        return appCompatEditText.text.toString().trim().isEmpty()
    }

    fun isEmailValid(email: String?): Boolean {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length in 7..13
        } else false
    }

    /*fun validateMobile(number: String): Boolean {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }*/

    /*fun isMobileValid(mobile: String?): Boolean {
        return (mobile!!.startsWith("04") || mobile.startsWith("05")
                || mobile.startsWith("4") || mobile.startsWith("5"))
    }

    fun isMobileValidLength(mobile: String?): Boolean {
        return mobile?.length!! == 10 || mobile.length == 9
    }*/

    fun isPasswordValid(password: String?): Boolean {
        return password?.length!! >= 6
    }

    fun isEqualTexts(
        appCompatEditText1: AppCompatEditText,
        appCompatEditText2: AppCompatEditText
    ): Boolean {
        return appCompatEditText2.text.toString().trim() == appCompatEditText1.text.toString()
            .trim()
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showAlert(context: Context, title: String, message: String) {

        try {
            val alertDialog = object :
                CommonDialog(
                    context = context,
                    theme = android.R.style.Theme_Translucent_NoTitleBar
                ) {
                override fun okClicked() {
                }
            }
            alertDialog.initDialog(title = title, message = message)
            alertDialog.setCancelable(true)
            alertDialog.show()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun delete(file: File) {
        if (file.isFile && file.exists()) {
            deleteOrThrow(file)
        } else {
            cleanDirectory(file)
            deleteOrThrow(file)
        }
    }

    fun hideKeyboard(view: View, context: Context) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showKeyboard(view: View, context: Context) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethodManager.SHOW_FORCED,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun deleteOrThrow(file: File) {
        if (file.exists()) {
            val isDeleted = file.delete()
            if (!isDeleted) {
                throw IOException(String.format("File %s can't be deleted", file.absolutePath))
            }
        }
    }
}