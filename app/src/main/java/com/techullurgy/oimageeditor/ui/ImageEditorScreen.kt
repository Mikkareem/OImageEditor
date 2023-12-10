package com.techullurgy.oimageeditor.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.techullurgy.oimageeditor.ui.components.EditableImage
import com.techullurgy.oimageeditor.ui.theme.OImageEditorTheme
import java.io.FileDescriptor


@Composable
fun ImageEditorScreen(
    viewModel: ImageEditorViewModel
) {
    OImageEditorTheme {
        val context = LocalContext.current

        val pickVisualMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { contentUri ->
            contentUri?.let { uri ->
                context.contentResolver.openFileDescriptor(uri, "r").use {
                    it?.let { parcelFileDescriptor ->
                        val fd: FileDescriptor = parcelFileDescriptor.fileDescriptor
                        val bitmap: Bitmap = BitmapFactory.decodeFileDescriptor(fd)
                        viewModel.setInputBitmap(bitmap)
                    }
                }
            }
        }

        val permissionRequestLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if(isGranted) {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }

        val localColors = if(isSystemInDarkTheme()) {
            OImageEditorColors()
        } else {
            OImageEditorColors(
                backgroundColor = Color(0xffe19898),
                buttonColor = Color(0xffa2678a)
            )
        }

        CompositionLocalProvider(LocalImageEditorColors provides localColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalImageEditorColors.current.backgroundColor),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                viewModel.inputBitmap.value?.let {
                    EditableImage(
                        bitmap = it.asImageBitmap(),
                        onCrop = viewModel::onCrop
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                viewModel.editedBitmap.value?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = null)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalImageEditorColors.current.buttonColor
                        ),
                        onClick = {
                            viewModel.saveEditedImageToExternalStorage(context)
                        }
                    ) {
                        Text(text = "Save Edited Image")
                    }
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalImageEditorColors.current.buttonColor
                    ),
                    onClick = {
                        if(readPermissionsGranted(context)) {
                            pickVisualMediaLauncher.launch(
                                PickVisualMediaRequest(
                                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        } else {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionRequestLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    }
                ) {
                    Text(text = "Pick Image")
                }
            }
        }
    }
}

private fun readPermissionsGranted(context: Context): Boolean {
    val readPermission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    return ContextCompat.checkSelfPermission(context, readPermission) == PackageManager.PERMISSION_GRANTED
}

data class OImageEditorColors(
    val backgroundColor: Color = Color(0xff614bc3),
    val buttonColor: Color = Color(0xff33bbc5)
)

val LocalImageEditorColors = compositionLocalOf { OImageEditorColors() }