package com.techullurgy.oimageeditor.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileDescriptor
import java.io.FileOutputStream

object StorageHelper {
    suspend fun saveImageToExternalStorage(context: Context, savableBitmap: Bitmap, outputFileName: String): Uri? = withContext(Dispatchers.IO) {

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, outputFileName)
            put(MediaStore.Images.ImageColumns.TITLE, outputFileName)
            put(MediaStore.Images.ImageColumns.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.DATE_MODIFIED, System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.DESCRIPTION, "Edited Image")
            put(MediaStore.Images.ImageColumns.WIDTH, savableBitmap.width)
            put(MediaStore.Images.ImageColumns.HEIGHT, savableBitmap.height)
            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.ImageColumns.IS_PENDING, true)
//            Need to put RelativePath, when we have custom folder structure like Pictures/Whatsapp etc..
//            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, "")
        }

        val contentVolumeUri: Uri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            As per scoped storage, we can both read from and write to vol_external_primary.
//            we can only read from the vol_external, we can't write to vol_external.
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val outputUri: Uri? = context.contentResolver.insert(contentVolumeUri, contentValues)

        outputUri?.let {
            context.contentResolver.openFileDescriptor(it, "w").use { parcelFileDescriptor ->
                val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
                fileDescriptor?.let { fd ->
                    val outputStream = FileOutputStream(fd)
                    if(savableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val updatedContent = ContentValues().apply {
                                put(MediaStore.Images.ImageColumns.IS_PENDING, false)
                            }
                            context.contentResolver.update(it, updatedContent, null)
                        }
                    }
                    it
                }
            }
        }
    }
}