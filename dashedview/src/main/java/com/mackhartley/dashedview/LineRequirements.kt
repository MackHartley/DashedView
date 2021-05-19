package com.mackhartley.dashedview

/**
 * Helper data class for calculating the points at which lines should be drawn.
 */
data class LineRequirements(
    val dashWidth: Float,
    val spaceBetweenDashes: Float,
    val dashAngle: Int,
    val viewWidth: Float,
    val viewHeight: Float
)