package com.mackhartley.dashedview

sealed class DashDirection(
    val isHorizontal: Boolean
) {
    class LeftToRight(isHorizontal: Boolean) : DashDirection(isHorizontal)
    class RightToLeft(isHorizontal: Boolean) : DashDirection(isHorizontal)
    object Vertical : DashDirection(false)
}