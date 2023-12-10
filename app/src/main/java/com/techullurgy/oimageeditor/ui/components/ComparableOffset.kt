package com.techullurgy.oimageeditor.ui.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

/***
 * This function is especially used in the context of Drag gesture, that the finger is actually moving on the screen
 */
operator fun Offset.compareTo(other: Offset): Int {
    return if(abs(this.x - other.x) >= 1.5f || abs(this.y - other.y) >= 1.5f) 0 else 1
}