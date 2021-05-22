package com.mackhartley.dashedview

/**
 * Contains all information required to draw a single dash (aka line).
 *
 * [Pair.first] contains the x value
 * [Pair.second] contains the y value
 */
data class DashInfo(
    val startPoint: Pair<Float, Float>,
    val endPoint: Pair<Float, Float>
)
