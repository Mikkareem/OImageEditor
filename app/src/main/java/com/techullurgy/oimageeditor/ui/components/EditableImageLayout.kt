package com.techullurgy.oimageeditor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints

@Composable
internal fun EditableImageLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) {measurables, constraints ->
        check(measurables.size in 1..2)

        val placeables = Array<Placeable?>(measurables.size) { null }

        placeables[0] = measurables[0].measure(constraints)
        val originalImageWidth = placeables[0]?.width!!
        val originalImageHeight = placeables[0]?.height!!

        if(measurables.size == 2) {
            placeables[1] = measurables[1].measure(Constraints.fixed(originalImageWidth, originalImageHeight))
        }

        layout(originalImageWidth, originalImageHeight) {
            placeables.map { placeable ->
                placeable?.place(0, 0)
            }
        }
    }
}
