package com.techullurgy.oimageeditor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
internal fun ImageCropperView(
    onCropMarkerChanged: (Pair<Offset, Offset>) -> Unit
) {

    var dragAmountX by remember { mutableStateOf(0f) }
    var dragAmountY by remember { mutableStateOf(0f) }
    var dragCurrentOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragStarted by remember { mutableStateOf(false) }

    var imageCropperState by rememberImageCropperState()

    var clippableRect: Rect = Rect.Zero

    val imageCropperMarkerTopLeftOffsetProvider: () -> ImageCropperState = remember {
        {
            val newLeft = (imageCropperState.cropperMarkerLeft + dragAmountX).coerceIn(0f, imageCropperState.cropperMarkerRight - 50f)
            val newTop = (imageCropperState.cropperMarkerTop + dragAmountY).coerceIn(0f, imageCropperState.cropperMarkerBottom - 50f)
            debug("Top Left")
            imageCropperState.copy(
                cropperMarkerLeft = newLeft,
                cropperMarkerTop = newTop
            )
        }
    }

    val imageCropperMarkerTopRightOffsetProvider: () -> ImageCropperState = remember {
        {
            val newTop = (imageCropperState.cropperMarkerTop + dragAmountY).coerceIn(0f, imageCropperState.cropperMarkerBottom - 50f)
            val newRight = (imageCropperState.cropperMarkerRight + dragAmountX).coerceIn(imageCropperState.cropperMarkerLeft + 50f, imageCropperState.maxSize.width)
            debug("Top Right")
            imageCropperState.copy(
                cropperMarkerTop = newTop,
                cropperMarkerRight = newRight
            )
        }
    }

    val imageCropperMarkerBottomLeftOffsetProvider: () -> ImageCropperState = remember {
        {
            val newLeft = (imageCropperState.cropperMarkerLeft + dragAmountX).coerceIn(0f, imageCropperState.cropperMarkerRight - 50f)
            val newBottom = (imageCropperState.cropperMarkerBottom + dragAmountY).coerceIn(imageCropperState.cropperMarkerTop + 50f, imageCropperState.maxSize.height)
            debug("Bottom Left")
            imageCropperState.copy(
                cropperMarkerLeft = newLeft,
                cropperMarkerBottom = newBottom
            )
        }
    }

    val imageCropperMarkerBottomRightOffsetProvider: () -> ImageCropperState = remember {
        {
            val newRight = (imageCropperState.cropperMarkerRight + dragAmountX).coerceIn(imageCropperState.cropperMarkerLeft + 50f, imageCropperState.maxSize.width)
            val newBottom = (imageCropperState.cropperMarkerBottom + dragAmountY).coerceIn(imageCropperState.cropperMarkerTop + 50f, imageCropperState.maxSize.height)
            debug("Bottom Right")
            imageCropperState.copy(
                cropperMarkerRight = newRight,
                cropperMarkerBottom = newBottom
            )
        }
    }

    val imageCropperDragProvider: () -> ImageCropperState = remember {
        {
            var newLeft = (imageCropperState.cropperMarkerLeft + dragAmountX).coerceIn(0f, imageCropperState.maxSize.width)
            var newTop = (imageCropperState.cropperMarkerTop + dragAmountY).coerceIn(0f, imageCropperState.maxSize.height)
            var newRight = (imageCropperState.cropperMarkerRight + dragAmountX).coerceIn(0f, imageCropperState.maxSize.width)
            var newBottom = (imageCropperState.cropperMarkerBottom + dragAmountY).coerceIn(0f, imageCropperState.maxSize.height)

            if(newLeft == 0f) {
                newRight = imageCropperState.cropperMarkerRight
            }
            if(newTop == 0f) {
                newBottom = imageCropperState.cropperMarkerBottom
            }
            if(newRight == imageCropperState.maxSize.width) {
                newLeft = imageCropperState.cropperMarkerLeft
            }
            if(newBottom == imageCropperState.maxSize.height) {
                newTop = imageCropperState.cropperMarkerTop
            }

            imageCropperState.copy(
                cropperMarkerLeft = newLeft,
                cropperMarkerTop = newTop,
                cropperMarkerRight = newRight,
                cropperMarkerBottom = newBottom
            )
        }
    }

    Canvas(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragStarted = true
                    },
                    onDragEnd = {
                        isDragStarted = false
                        onCropMarkerChanged(Pair(clippableRect.topLeft, clippableRect.bottomRight))
                    }
                ) { change, dragAmount ->
                    change.consume()

                    if (change.position.compareTo(change.previousPosition) == 0) {
                        dragAmountX = dragAmount.x
                        dragAmountY = dragAmount.y
                        dragCurrentOffset = change.position
                        isDragStarted = true
                    } else {
                        isDragStarted = false
                    }
                }
            }
    ) {
        if(!imageCropperState.isInitialMarkersSet) {
            imageCropperState = imageCropperState.copy(
                cropperMarkerLeft = center.x - size.width / 4f,
                cropperMarkerTop = center.y - size.height / 4f,
                cropperMarkerRight = center.x + size.width / 4f,
                cropperMarkerBottom = center.y + size.height / 4f,
                maxSize = size,
                isInitialMarkersSet = true
            )
            debug("InitialState $imageCropperState")
        }

        val cropperMarkerLeft = imageCropperState.cropperMarkerLeft
        val cropperMarkerTop = imageCropperState.cropperMarkerTop
        val cropperMarkerRight = imageCropperState.cropperMarkerRight
        val cropperMarkerBottom = imageCropperState.cropperMarkerBottom

        clippableRect = Rect(
            cropperMarkerLeft, cropperMarkerTop, cropperMarkerRight, cropperMarkerBottom
        )
        val touchOffset = 16.dp.toPx()

        val topLeftRect = Rect(
            left = clippableRect.topLeft.x - touchOffset,
            top = clippableRect.topLeft.y - touchOffset,
            right = clippableRect.topLeft.x + touchOffset,
            bottom = clippableRect.topLeft.y + touchOffset
        )

        val topRightRect = Rect(
            left = clippableRect.topRight.x - touchOffset,
            top = clippableRect.topRight.y - touchOffset,
            right = clippableRect.topRight.x + touchOffset,
            bottom = clippableRect.topRight.y + touchOffset
        )

        val bottomLeftRect = Rect(
            left = clippableRect.bottomLeft.x - touchOffset,
            top = clippableRect.bottomLeft.y - touchOffset,
            right = clippableRect.bottomLeft.x + touchOffset,
            bottom = clippableRect.bottomLeft.y + touchOffset
        )

        val bottomRightRect = Rect(
            left = clippableRect.bottomRight.x - touchOffset,
            top = clippableRect.bottomRight.y - touchOffset,
            right = clippableRect.bottomRight.x + touchOffset,
            bottom = clippableRect.bottomRight.y + touchOffset
        )

        if(isDragStarted) {
            if(topLeftRect.contains(dragCurrentOffset)) {
                imageCropperState = imageCropperMarkerTopLeftOffsetProvider()
            } else if(topRightRect.contains(dragCurrentOffset)) {
                imageCropperState = imageCropperMarkerTopRightOffsetProvider()
            } else if(bottomLeftRect.contains(dragCurrentOffset)) {
                imageCropperState = imageCropperMarkerBottomLeftOffsetProvider()
            } else if(bottomRightRect.contains(dragCurrentOffset)) {
                imageCropperState = imageCropperMarkerBottomRightOffsetProvider()
            } else if(clippableRect.contains(dragCurrentOffset)) {
                imageCropperState = imageCropperDragProvider()
            }
        }

        clipRect(
            clippableRect.left,
            clippableRect.top,
            clippableRect.right,
            clippableRect.bottom,
            clipOp = ClipOp.Difference
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.5f))
        }

        val topLeftCropMarkerPath = Path().apply {
            moveTo(topLeftRect.center.x, topLeftRect.bottom)
            lineTo(topLeftRect.center.x, topLeftRect.center.y)
            lineTo(topLeftRect.right, topLeftRect.center.y)
        }

        val topRightCropMarkerPath = Path().apply {
            moveTo(topRightRect.center.x, topRightRect.bottom)
            lineTo(topRightRect.center.x, topRightRect.center.y)
            lineTo(topRightRect.left, topRightRect.center.y)
        }

        val bottomLeftCropMarkerPath = Path().apply {
            moveTo(bottomLeftRect.center.x, bottomLeftRect.top)
            lineTo(bottomLeftRect.center.x, bottomLeftRect.center.y)
            lineTo(bottomLeftRect.right, bottomLeftRect.center.y)
        }

        val bottomRightCropMarkerPath = Path().apply {
            moveTo(bottomRightRect.center.x, bottomRightRect.top)
            lineTo(bottomRightRect.center.x, bottomRightRect.center.y)
            lineTo(bottomRightRect.left, bottomRightRect.center.y)
        }

        drawPath(path = topLeftCropMarkerPath, color = Color.Yellow, style = Stroke(width = 8.dp.toPx()))
        drawPath(path = topRightCropMarkerPath, color = Color.Yellow, style = Stroke(width = 8.dp.toPx()))
        drawPath(path = bottomLeftCropMarkerPath, color = Color.Yellow, style = Stroke(width = 8.dp.toPx()))
        drawPath(path = bottomRightCropMarkerPath, color = Color.Yellow, style = Stroke(width = 8.dp.toPx()))
    }
}

private data class ImageCropperState(
    val cropperMarkerLeft: Float,
    val cropperMarkerTop: Float,
    val cropperMarkerRight: Float,
    val cropperMarkerBottom: Float,
    val isInitialMarkersSet: Boolean = false,
    val maxSize: Size = Size.Zero
) {
    override fun toString(): String {
        return "ImageCropperState(left=$cropperMarkerLeft, top=$cropperMarkerTop, right=$cropperMarkerRight bottom=$cropperMarkerBottom) "
    }
}

@Composable
private fun rememberImageCropperState(initialState: ImageCropperState = defaultImageCropperState) = remember {
    mutableStateOf(initialState)
}

private val defaultImageCropperState: ImageCropperState = ImageCropperState(
    0f,
    0f,
    0f,
    0f
)

const val DEBUG_MODE = false
fun debug(message: String) {
    if(DEBUG_MODE) {
        println(message)
    }
}