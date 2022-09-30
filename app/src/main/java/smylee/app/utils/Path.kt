package smylee.app.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File


object Path {

    /*private val outputPath: String
        get() {
            val pathMain = Environment.getExternalStorageDirectory()
                .toString() + File.separator + Constants.App_Folder + File.separator + Constants.AudioFile + File.separator

            val folder = File(pathMain)
            if (!folder.exists())
                folder.mkdirs()

            return pathMain
        }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }*/

    /*fun getRealPathFromURI(context: Context, contentUri: Uri?): String {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun copyFileToExternalStorage(resourceId: Int, resourceName: String, context: Context): File {
        val pathSDCard = outputPath + resourceName
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            inputStream.toFile(pathSDCard)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return File(pathSDCard)
    }*/

    fun createFileDir(context: Context, dirName: String): File? {
        val filePath: String = if (FileUtils.isMountedSDCard()) {
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + FileUtils.ROOT_FOLDER + File.separator + dirName
        } else {
            context.cacheDir.path + File.separator + dirName
        }
        val destDir = File(filePath)
        if (!destDir.exists()) {
            val isCreate = destDir.mkdirs()
            Log.i("FileUtils", "$filePath has created. $isCreate")
        }
        return destDir
    }

    /*fun getFilesFromDirectory(pathDirectory: String, filname: String): String? {
        val directory = File(pathDirectory)
        val files = directory.listFiles()
        var name_fiile: String? = ""

        if (files != null && files.size > 0) {
            for (files in directory.listFiles()) {
                name_fiile = files.absolutePath
            }
        }
        return name_fiile
    }*/
}