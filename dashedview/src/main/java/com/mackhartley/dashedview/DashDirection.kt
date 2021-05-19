package com.mackhartley.dashedview

/**
 * Indicates the direction that lines are pointing.
 *
 * [LeftToRight] means the angle of all lines drawn is between 0 and 89 degrees inclusive.
 * [Vertical] means the angle of all lines drawn is equal to 90 degrees.
 * [RightToLeft] means the angle of all lines drawn is between 91 and 179 degrees inclusive.
 *
 * [isHorizontal] indicates that a given configuration has an angle of 0 degrees. This is important
 * to know for certain trigonometric calculations.
 */
sealed class DashDirection(
    val isHorizontal: Boolean
) {
    class LeftToRight(isHorizontal: Boolean) : DashDirection(isHorizontal)
    class RightToLeft(isHorizontal: Boolean) : DashDirection(isHorizontal)
    object Vertical : DashDirection(false)
}


// todo write unit test
fun getDashDirection(dashAngle: Int): DashDirection {
    return when {
        dashAngle == 0 -> DashDirection.LeftToRight(true)
        dashAngle < 90 -> DashDirection.LeftToRight(false)
        dashAngle == 90 -> DashDirection.Vertical
        else -> DashDirection.RightToLeft(false)
    }
}