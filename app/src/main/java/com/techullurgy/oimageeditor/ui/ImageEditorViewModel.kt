package com.techullurgy.oimageeditor.ui

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techullurgy.oimageeditor.data.StorageHelper
import com.techullurgy.oimageeditor.ui.components.services.ImageEditor
import com.techullurgy.oimageeditor.ui.components.services.OImageEditorImpl
import kotlinx.coroutines.launch

class ImageEditorViewModel: ViewModel() {
    private var _inputBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val inputBitmap: State<Bitmap?>
        get() = _inputBitmap

    private var _editedBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val editedBitmap: State<Bitmap?>
        get() = _editedBitmap

    private val imageEditor: ImageEditor

    init {
        imageEditor = OImageEditorImpl()
    }

    fun onCrop(cropRect: Rect) {
        _editedBitmap.value = imageEditor.crop(inputBitmap.value!!, cropRect)
    }

    fun setInputBitmap(bitmap: Bitmap?) {
        _inputBitmap.value = bitmap
    }

    fun saveEditedImageToExternalStorage(context: Context) {
        _editedBitmap.value?.let {
            viewModelScope.launch {
                val fileName = "output_${System.currentTimeMillis()}.jpg"
                val outputUri = StorageHelper.saveImageToExternalStorage(context, it, fileName)
                outputUri?.let {
                    Toast.makeText(context, "Cropped Image saved successfully", Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(context, "Can't save image file", Toast.LENGTH_LONG).show()
            }
        }
    }
}