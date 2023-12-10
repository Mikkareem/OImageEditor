package com.techullurgy.oimageeditor.ui.components.services

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect

interface ImageEditor {
    fun crop(bitmap: Bitmap, cropRect: Rect): Bitmap
}