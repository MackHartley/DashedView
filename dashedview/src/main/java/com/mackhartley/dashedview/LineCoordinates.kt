package com.mackhartley.dashedview

/**
 * Contains all information required to draw a single line.
 *
 * [Pair.first] contains the x value
 * [Pair.second] contains the y value
 */
data class LineCoordinates(
    val startPoint: Pair<Float, Float>,
    val endPoint: Pair<Float, Float>
)
