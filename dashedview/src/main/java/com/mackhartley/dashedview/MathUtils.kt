package com.mackhartley.dashedview

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Calculates the X axis translation required to get the end point for any start point.
 */
internal fun calculateEndPointXTranslation(
    dashAngle: Int,
    viewHeight: Float
): Float {
    // Check for 0 degrees. If present, just return viewHeight to avoid a divide by 0 error
    if (getDashDirection(dashAngle).isHorizontal) return viewHeight
    val dashAngleRadians = Math.toRadians(dashAngle.toDouble())
    val endPointXTranslation = viewHeight / tan(dashAngleRadians)
    return endPointXTranslation.toFloat()
}

/**
 * Modifies a provided [xTranslation] value so it can be properly applied to a start point or
 * end point based on the current dash angle.
 */
internal fun calculateModifiedXTranslation(
    xTranslation: Double,
    dashAngle: Int,
    isEndPoint: Boolean
): Float {
    // Modify the translation based on the angle at which dashes are drawn.
    val modifiedXTranslation = when (getDashDirection(dashAngle)) {
        is DashDirection.LeftToRight -> xTranslation * -1
        is DashDirection.RightToLeft -> xTranslation
        is DashDirection.Vertical -> 0.0
    }

    // One more modification required. Start points and end points need opposite x translations.
    val finalModifiedXTranslation =
        if (isEndPoint) modifiedXTranslation * -1
        else modifiedXTranslation

    return finalModifiedXTranslation.toFloat()
}

/**
 * Calculates how much horizontal space should be between two dashes.
 *
 * This method should not be called for horizontal lines. If it is, it will return a safe value
 * to avoid a divide by 0 error.
 */
internal fun calculateHorizontalOffset(dashAngle: Int, width: Float): Float {
    if (getDashDirection(dashAngle).isHorizontal) return width
    val radians = Math.toRadians((90 - dashAngle).toDouble())
    return width / (cos(radians)).toFloat()
}

/**
 * Calculates how much vertical space should be between two dashes.
 *
 * This method should not be called for vertical lines. If it is, it will return a safe value
 * to avoid a divide by 0 error.
 */
internal fun calculateVerticalOffset(dashAngle: Int, width: Float): Float {
    if (getDashDirection(dashAngle) is DashDirection.Vertical) return width
    val complementaryAngle = Math.toRadians(abs(90 - dashAngle).toDouble())
    val halfVerticalLengthDashCrossSection = width / (2 * sin(complementaryAngle))
    return halfVerticalLengthDashCrossSection.toFloat() * 2f
}