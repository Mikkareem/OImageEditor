package com.techullurgy.oimageeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.techullurgy.oimageeditor.ui.ImageEditorScreen
import com.techullurgy.oimageeditor.ui.ImageEditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel by viewModels<ImageEditorViewModel>()
            ImageEditorScreen(viewModel = viewModel)
        }
    }
}