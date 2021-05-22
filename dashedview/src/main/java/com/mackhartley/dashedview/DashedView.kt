package com.mackhartley.dashedview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class DashedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Instance state
    private var dashWidth = DEFAULT_WIDTH
    private var spaceBetweenDashes = DEFAULT_SPACE_BETWEEN_DASHES
    private var dashAngle = DEFAULT_DASH_ANGLE
    private var cornerRadius = DEFAULT_CORNER_RADIUS
    @ColorInt private var dashColor = DEFAULT_DASH_COLOR
    private var dashColorGenerator: DashColorGenerator? = null

    private var lastWidth = width // Used for keeping track of view size
    private var lastHeight = height // Used for keeping track of view size

    private val dashPaint by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = dashColor
            strokeWidth = dashWidth
            isAntiAlias = true
        }
    }

    private val roundedCornersClipPath by lazy { // This path is used to clip the progress background and drawable to the desired corner radius
        Path().apply {
            addRoundRect(
                VIEW_LEFT,
                VIEW_TOP,
                lastWidth.toFloat(),
                lastHeight.toFloat(),
                floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius),
                Path.Direction.CW
            )
        }
    }

    companion object {
        const val DEFAULT_WIDTH = 12f
        const val DEFAULT_SPACE_BETWEEN_DASHES = 12f
        const val DEFAULT_DASH_ANGLE = 45 // Measured in degrees. Min 0, max 179. A value of 0 Degrees points to the right side of the view
        const val DEFAULT_CORNER_RADIUS = 0f
        const val DEFAULT_DASH_COLOR = Color.GRAY
        const val VIEW_LEFT = 0f
        const val VIEW_TOP = 0f
    }

    init {
        val attrRefs = context.obtainStyledAttributes(attrs, R.styleable.DashedView)

        val requestedWidth = attrRefs.getDimension(R.styleable.DashedView_dvDashWidth, DEFAULT_WIDTH)
        dashWidth = parseRequestedDashWidth(requestedWidth)

        val requestedAngle = attrRefs.getInteger(R.styleable.DashedView_dvDashAngle, DEFAULT_DASH_ANGLE)
        dashAngle = parseRequestedDashAngle(requestedAngle)

        spaceBetweenDashes = attrRefs.getDimension(R.styleable.DashedView_dvSpaceBetweenDashes, DEFAULT_SPACE_BETWEEN_DASHES)
        dashColor = attrRefs.getColor(R.styleable.DashedView_dvDashColor, DEFAULT_DASH_COLOR)
        cornerRadius = attrRefs.getDimension(R.styleable.DashedView_dvViewCornerRadius, DEFAULT_CORNER_RADIUS)
        attrRefs.recycle()
    }

    /**
     * [DashedView] only allows angles between 0 and 179 inclusive. Values outside of that range will
     * have a modulus operation applied to them.
     */
    private fun parseRequestedDashAngle(requestedAngle: Int): Int {
        return requestedAngle % 180
    }

    private fun parseRequestedDashWidth(requestedWidth: Float): Float {
        return if (requestedWidth <= 0f) 1f
        else requestedWidth
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {

            // Apply the requested rounded corner value
            canvas.clipPath(roundedCornersClipPath)

            // Get list of coordinates for dashes that need to be drawn
            val dashesOriginatingFromXAxis = getDashesOriginatingFromXAxis(
                dashWidth = dashWidth,
                spaceBetweenDashes = spaceBetweenDashes,
                dashAngle = dashAngle,
                viewWidth = width.toFloat(),
                viewHeight = height.toFloat()
            )
            val dashesOriginatingFromYAxis = getDashesOriginatingFromYAxis(
                dashWidth = dashWidth,
                spaceBetweenDashes = spaceBetweenDashes,
                dashAngle = dashAngle,
                viewWidth = width.toFloat(),
                viewHeight = height.toFloat()
            )
            val dashDirection = getDashDirection(dashAngle)
            val allDashesToDraw =
                if (dashDirection.isHorizontal) dashesOriginatingFromYAxis // If horizontal, only draw dashes originating from Y axis
                else when (dashDirection) {
                    is DashDirection.LeftToRight -> dashesOriginatingFromYAxis.reversed() + dashesOriginatingFromXAxis
                    is DashDirection.RightToLeft -> dashesOriginatingFromXAxis.reversed() + dashesOriginatingFromYAxis
                    is DashDirection.Vertical -> dashesOriginatingFromXAxis // If vertical, only draw dashes originating from X axis
                }

            // Loop through coordinate list and draw dashes
            for ((index, dashCoordinates) in allDashesToDraw.withIndex()) {
                val startPoint = dashCoordinates.startPoint
                val endPoint = dashCoordinates.endPoint
                updatePaintColorIfNeeded(dashColorGenerator, index, allDashesToDraw.size)

                canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint)
            }
        }
    }

    private fun updatePaintColorIfNeeded(
        dashColorGenerator: DashColorGenerator?,
        index: Int,
        numDashes: Int
    ) {
        if (dashColorGenerator != null) {
            dashPaint.color = dashColorGenerator.getPaintColor(index, numDashes)
        }
    }

    /**
     * Generates dashes with start points originating from the X axis.
     */
    private fun getDashesOriginatingFromXAxis(
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewWidth: Float,
        viewHeight: Float
    ): List<DashInfo> {

        // Check if horizontal config (0 degrees). If so, no dashes drawn from x axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection.isHorizontal) return emptyList()

        // Calculate start points
        val startPoints = mutableListOf<Pair<Float, Float>>()
        val startYPosition = viewHeight
        val dashHorizontalOffset = calculateHorizontalOffset(dashAngle, dashWidth)
        val spaceHorizontalOffset = calculateHorizontalOffset(dashAngle, spaceBetweenDashes)

        var curXPosition: Float
        when (dashDirection) {
            is DashDirection.LeftToRight,
            is DashDirection.Vertical -> {
                curXPosition = VIEW_LEFT
                while (curXPosition <= viewWidth) {
                    startPoints.add(Pair(curXPosition, startYPosition))
                    curXPosition += abs(dashHorizontalOffset + spaceHorizontalOffset)
                }
            }
            is DashDirection.RightToLeft -> { // If the dashes are pointing from right to left, then start drawing dashes from the bottom right corner of the view
                curXPosition = viewWidth
                while (curXPosition >= VIEW_LEFT) {
                    startPoints.add(Pair(curXPosition, startYPosition))
                    curXPosition -= abs(dashHorizontalOffset + spaceHorizontalOffset)
                }
            }
        }
        startPoints.add(Pair(curXPosition, startYPosition)) // Add one more dash to ensure the view is fully covered by dashes

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get list of dash coordinates
        val endPointXTranslation = calculateEndPointXTranslation(dashAngle, viewHeight)
        val dashCoordinates = startPoints.map {
            DashInfo(
                startPoint = it,
                endPoint =
                    generateEndPoint(
                        startPoint = it,
                        dashAngle = dashAngle,
                        endPointXTranslation = endPointXTranslation,
                        endPointYValue = VIEW_TOP,
                        viewWidth = viewWidth
                    )
            )
        }

        // Translate start and end points so all 4 corners of dash are drawn outside of the view
        val elongatedDashCoordinates = elongateDashesOriginatingFromXAxis(
            initialPositions = dashCoordinates,
            dashAngle = dashAngle,
            dashWidth = dashWidth
        )

        return elongatedDashCoordinates
    }

    /**
     * Generates dashes with start points originating on the Y axis.
     */
    private fun getDashesOriginatingFromYAxis(
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewWidth: Float,
        viewHeight: Float
    ): List<DashInfo> {

        // Check if vertical config (90 degrees). If so, no dashes should be drawn from the y axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection is DashDirection.Vertical) return emptyList()

        // Calculate start points
        val startPositions = mutableListOf<Pair<Float, Float>>()
        val startXPosition = if (dashDirection is DashDirection.LeftToRight) VIEW_LEFT else viewWidth

        var curYPosition = viewHeight

        val dashVerticalOffset = calculateVerticalOffset(dashAngle, dashWidth)
        val spaceVerticalOffset = calculateVerticalOffset(dashAngle, spaceBetweenDashes)
        if (!dashDirection.isHorizontal) { // If dashes will be drawn from the x axis, then skip drawing a dash for the first y position because it is equal to the first x position
            curYPosition -= abs(dashVerticalOffset + spaceVerticalOffset)
        }
        while (curYPosition >= VIEW_TOP) {
            startPositions.add(Pair(startXPosition, curYPosition))
            curYPosition -= abs(dashVerticalOffset + spaceVerticalOffset)
        }
        startPositions.add(Pair(startXPosition, curYPosition)) // Add one more dash to ensure the view is fully covered by dashes

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get list of dash coordinates
        val transition = calculateEndPointXTranslation(dashAngle, viewHeight)
        val dashCoordinates = startPositions.map {
            DashInfo(
                startPoint = Pair(
                    it.first,
                    it.second
                ),
                endPoint = generateEndPoint(
                    startPoint = it,
                    dashAngle = dashAngle,
                    endPointXTranslation = transition,
                    endPointYValue = it.second - viewHeight,// Subtract view height because each of these dashes has a different start point Y value
                    viewWidth = viewWidth
                )
            )
        }

        // Translate start and end points so all 4 corners of dash are drawn outside of the view
        val elongatedDashCoordinates = elongateDashesOriginatingFromYAxis(
            initialPositions = dashCoordinates,
            dashAngle = dashAngle,
            dashWidth = dashWidth
        )

        return elongatedDashCoordinates
    }

    /**
     * Elongate dashes enough so that all 4 corners of each dash are extended out of the view
     * canvas. This ensures the corners of the dashes/lines are not visible. This is more important
     * when dashes get thicker and resemble rectangles instead of dashes/lines.
     *
     * Affects Dashes that originate from the X axis.
     */
    private fun elongateDashesOriginatingFromXAxis(
        initialPositions: List<DashInfo>,
        dashAngle: Int,
        dashWidth: Float
    ): List<DashInfo> {
        val hypotenuseRadians = Math.toRadians((abs(90 - dashAngle).toDouble()))
        val translationHypotenuse = (dashWidth * tan((hypotenuseRadians))) / 2 // This is the amount the point must move
        val xTranslation = translationHypotenuse * sin(Math.toRadians((abs(90 - dashAngle).toDouble())))
        val yTranslation = translationHypotenuse * cos(Math.toRadians((abs(90 - dashAngle).toDouble())))

        return initialPositions.map {
            DashInfo(
                Pair(
                    it.startPoint.first + calculateModifiedXTranslation(xTranslation, dashAngle, false),
                    it.startPoint.second + yTranslation.toFloat()
                ),
                Pair(
                    it.endPoint.first + calculateModifiedXTranslation(xTranslation, dashAngle, true),
                    it.endPoint.second - yTranslation.toFloat()
                )
            )
        }
    }

    /**
     * Elongate dashes enough so that all 4 corners of each dash are extended out of the view
     * canvas. This ensures the corners of the dashes/lines are not visible. This is more important
     * when dashes get thicker and resemble rectangles instead of dashes/lines.
     *
     * Affects Dashes that originate from the Y axis.
     */
    private fun elongateDashesOriginatingFromYAxis(
        initialPositions: List<DashInfo>,
        dashAngle: Int,
        dashWidth: Float
    ): List<DashInfo> {
        val hypotenuseRadians = Math.toRadians(dashAngle.toDouble())
        val translationHypotenuse = (dashWidth * tan(hypotenuseRadians)) / 2 // This is the amount the point must move
        val xTranslation = abs(translationHypotenuse * cos(hypotenuseRadians)).toFloat()
        val yTranslation = abs(translationHypotenuse * sin(hypotenuseRadians)).toFloat()

        // Depending on the direction of dashes the translation value needs to be negative
        val xTranslationModifier = when (getDashDirection(dashAngle)) {
            is DashDirection.LeftToRight -> -1
            is DashDirection.RightToLeft -> 1
            DashDirection.Vertical -> 0
        }

        return initialPositions.map {
            DashInfo(
                Pair(
                    it.startPoint.first + (xTranslation * xTranslationModifier),
                    it.startPoint.second + yTranslation
                ),
                it.endPoint // End point can remain unchanged for dashes drawn from Y Axis. Their top corners wont show
            )
        }
    }

    /**
     * Generates the endpoint for a given start point based on the provided constraints.
     */
    private fun generateEndPoint(
        startPoint: Pair<Float, Float>,
        dashAngle: Int,
        endPointXTranslation: Float,
        endPointYValue: Float,
        viewWidth: Float
    ): Pair<Float, Float> {
        return if (getDashDirection(dashAngle).isHorizontal)
            Pair(viewWidth, startPoint.second)
        else
            Pair(startPoint.first + endPointXTranslation, endPointYValue)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastWidth = w
        lastHeight = h
    }

    fun setDashColorGenerator(newDashColorGenerator: DashColorGenerator) {
        dashColorGenerator = newDashColorGenerator
    }
}