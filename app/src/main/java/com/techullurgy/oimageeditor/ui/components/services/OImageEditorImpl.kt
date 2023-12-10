package com.techullurgy.oimageeditor.ui.components.services

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt

class OImageEditorImpl: ImageEditor {
    override fun crop(bitmap: Bitmap, cropRect: Rect): Bitmap {
        if(cropRect.left < 0 || cropRect.top < 0 || cropRect.right < 0 || cropRect.bottom < 0
            || cropRect.size == Size.Zero
            || cropRect.left > bitmap.width
            || cropRect.top > bitmap.height
            || cropRect.right > bitmap.width
            || cropRect.bottom > bitmap.height
            || cropRect.width > bitmap.width
            || cropRect.height > bitmap.height
        ) {
            throw IllegalArgumentException("cropRect is not matched with Source Bitmap")
        }

        return Bitmap.createBitmap(
            bitmap,
            cropRect.left.roundToInt(),
            cropRect.top.roundToInt(),
            cropRect.width.roundToInt(),
            cropRect.height.roundToInt()
        )
    }
}