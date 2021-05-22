package com.mackhartley.dashedview

import org.junit.Test

import org.junit.Assert.*

class DashedViewHelpersTest {

    companion object {
        const val DELTA = 0.0001f
    }

    @Test
    fun get_dash_direction_left_right_correctly() {
        val dashDirection = getDashDirection(45)
        assertTrue(dashDirection is DashDirection.LeftToRight)
        assertTrue(!dashDirection.isHorizontal)
    }

    @Test
    fun get_dash_direction_left_right_horizontal_correctly() {
        val dashDirection = getDashDirection(0)
        assertTrue(dashDirection is DashDirection.LeftToRight)
        assertTrue(dashDirection.isHorizontal)
    }

    @Test
    fun get_dash_direction_right_left_correctly() {
        val dashDirection = getDashDirection(135)
        assertTrue(dashDirection is DashDirection.RightToLeft)
        assertTrue(!dashDirection.isHorizontal)
    }

    @Test
    fun get_dash_direction_vertical_correctly() {
        val dashDirection = getDashDirection(90)
        assertTrue(dashDirection is DashDirection.Vertical)
    }

    @Test
    fun calculate_end_point_x_trans_left_right() {
        val angle = 45
        val viewHeight = 100f
        val xTranslation = calculateEndPointXTranslation(angle, viewHeight)
        assertEquals(xTranslation, 100f)
    }

    @Test
    fun calculate_end_point_x_trans_right_left() {
        val angle = 135
        val viewHeight = 100f
        val xTranslation = calculateEndPointXTranslation(angle, viewHeight)
        assertEquals(xTranslation, -100f)
    }

    @Test
    fun calculate_end_point_x_trans_vertical() {
        val angle = 90
        val viewHeight = 100f
        val xTranslation = calculateEndPointXTranslation(angle, viewHeight)
        assertEquals(xTranslation, 0f, DELTA)
    }

    @Test
    fun calculate_end_point_x_trans_horizontal() {
        val angle = 0
        val viewHeight = 100f
        val xTranslation = calculateEndPointXTranslation(angle, viewHeight)
        assertEquals(xTranslation, viewHeight)
    }

    @Test
    fun calculate_modified_x_trans_start_point_left_right() {
        val xTranslation = 5.0
        val dashAngle = 45
        val isEndPoint = false
        val modifiedValue = calculateModifiedXTranslation(xTranslation, dashAngle, isEndPoint)
        assertEquals(modifiedValue, -5.0f)
    }

    @Test
    fun calculate_modified_x_trans_start_point_right_left() {
        val xTranslation = 5.0
        val dashAngle = 135
        val isEndPoint = false
        val modifiedValue = calculateModifiedXTranslation(xTranslation, dashAngle, isEndPoint)
        assertEquals(modifiedValue, 5.0f)
    }

    @Test
    fun calculate_modified_x_trans_end_point_left_right() {
        val xTranslation = 5.0
        val dashAngle = 45
        val isEndPoint = true
        val modifiedValue = calculateModifiedXTranslation(xTranslation, dashAngle, isEndPoint)
        assertEquals(modifiedValue, 5.0f)
    }

    @Test
    fun calculate_modified_x_trans_end_point_right_left() {
        val xTranslation = 5.0
        val dashAngle = 135
        val isEndPoint = true
        val modifiedValue = calculateModifiedXTranslation(xTranslation, dashAngle, isEndPoint)
        assertEquals(modifiedValue, -5.0f)
    }

    @Test
    fun calculate_modified_x_trans_vertical() {
        val xTranslation = 5.0
        val dashAngle = 90
        val isEndPoint = true
        val modifiedValue = calculateModifiedXTranslation(xTranslation, dashAngle, isEndPoint)
        assertEquals(modifiedValue, 0f, DELTA)
    }

    @Test
    fun calculate_horizontal_offset_left_right_dash() {
        val angle = 60
        val width = 10f
        val horizontalOffset = calculateHorizontalOffset(angle, width)
        assertEquals(horizontalOffset, 11.547006f, DELTA)
    }

    @Test
    fun calculate_horizontal_offset_right_left_dash() {
        val angle = 120
        val width = 10f
        val horizontalOffset = calculateHorizontalOffset(angle, width)
        assertEquals(horizontalOffset, 11.547006f, DELTA)
    }

    @Test
    fun calculate_horizontal_offset_vertical_dash() {
        val angle = 90
        val width = 10f
        val horizontalOffset = calculateHorizontalOffset(angle, width)
        assertEquals(horizontalOffset, 10.0f, DELTA)
    }

    @Test
    fun calculate_horizontal_offset_horizontal_dash() {
        val angle = 0
        val width = 10f
        val horizontalOffset = calculateHorizontalOffset(angle, width)
        assertEquals(horizontalOffset, 10.0f, DELTA)
    }

    @Test
    fun calculate_vertical_offset_left_right_dash() {
        val angle = 60
        val width = 10f
        val horizontalOffset = calculateVerticalOffset(angle, width)
        assertEquals(horizontalOffset, 20.0f, DELTA)
    }

    @Test
    fun calculate_vertical_offset_right_left_dash() {
        val angle = 60
        val width = 10f
        val horizontalOffset = calculateVerticalOffset(angle, width)
        assertEquals(horizontalOffset, 20.0f, DELTA)
    }

    @Test
    fun calculate_vertical_offset_vertical_dash() {
        val angle = 90
        val width = 10f
        val horizontalOffset = calculateVerticalOffset(angle, width)
        assertEquals(horizontalOffset, 10.0f, DELTA)
    }

    @Test
    fun calculate_vertical_offset_horizontal_dash() {
        val angle = 0
        val width = 10f
        val horizontalOffset = calculateVerticalOffset(angle, width)
        assertEquals(horizontalOffset, 10.0f, DELTA)
    }
}