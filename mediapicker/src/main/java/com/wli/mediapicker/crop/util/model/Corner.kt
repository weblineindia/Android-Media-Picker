package com.wli.mediapicker.crop.util.model

import com.wli.mediapicker.crop.util.model.Corner.BOTTOM_LEFT
import com.wli.mediapicker.crop.util.model.Corner.BOTTOM_RIGHT
import com.wli.mediapicker.crop.util.model.Corner.NONE
import com.wli.mediapicker.crop.util.model.Corner.TOP_LEFT
import com.wli.mediapicker.crop.util.model.Corner.TOP_RIGHT

enum class Corner {
    NONE,
    TOP_RIGHT,
    TOP_LEFT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
}

fun Corner.opposite() {
    when (this) {
        TOP_RIGHT -> BOTTOM_LEFT
        TOP_LEFT -> BOTTOM_RIGHT
        BOTTOM_RIGHT -> TOP_LEFT
        BOTTOM_LEFT -> TOP_RIGHT
        NONE -> NONE
    }
}