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

// todo need to be able to set dash color interface
// todo can mention the library is efficienct. Dashes computed with minimal sin, cos, tan calls
// todo decide between dash and line language
// todo check all doc string descriptions
// todo improvement: Could limit max length of lines. For low dash angles such as 1 - 5 the lines are drawn quite far outside of the screen.

// Make git project:
// Todo would be nice to be able to set an outline stroke with a custom width and color


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

    private var lastWidth = width // Used for keeping track of view size
    private var lastHeight = height // Used for keeping track of view size

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

    private val dashPaint by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = dashColor
            strokeWidth = dashWidth
            isAntiAlias = true
        }
    }

    private val dashPaint2 by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = Color.GREEN
            strokeWidth = dashWidth
            isAntiAlias = true
        }
    }
    private val dashPaint3 by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = Color.BLUE
            strokeWidth = dashWidth
            isAntiAlias = true
        }
    }

    companion object {
        const val DEFAULT_WIDTH = 4f
        const val DEFAULT_SPACE_BETWEEN_DASHES = 4f
        const val DEFAULT_DASH_ANGLE = 45 // Measured in degrees. Min 0, max 179. A value of 0 Degrees points to the right side of the view
        const val DEFAULT_CORNER_RADIUS = 0f
        const val DEFAULT_DASH_COLOR = Color.GRAY

        const val VIEW_LEFT = 0f
        const val VIEW_TOP = 0f
    }

    init {
        val attrRefs = context.obtainStyledAttributes(attrs, R.styleable.DashedView)

        val requestedWidth = attrRefs.getDimension(R.styleable.DashedView_dashWidth, DEFAULT_WIDTH)
        dashWidth = parseRequestedDashWidth(requestedWidth)

        val requestedAngle = attrRefs.getInteger(R.styleable.DashedView_dashAngle, DEFAULT_DASH_ANGLE)
        dashAngle = parseRequestedDashAngle(requestedAngle)

        spaceBetweenDashes = attrRefs.getDimension(R.styleable.DashedView_spaceBetweenDashes, DEFAULT_SPACE_BETWEEN_DASHES)
        dashColor = attrRefs.getColor(R.styleable.DashedView_dashColor, DEFAULT_DASH_COLOR)
        cornerRadius = attrRefs.getDimension(R.styleable.DashedView_cornerRadius, DEFAULT_CORNER_RADIUS)
        attrRefs.recycle()
    }

    /**
     * DashedView only allows angles between 0 and 179 inclusive. Values outside of that range will
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

            // Get list of coordinates for lines that need to be drawn
            val linesOriginatingFromXAxis = getLinesOriginatingFromXAxis(
                dashWidth = dashWidth,
                spaceBetweenDashes = spaceBetweenDashes,
                dashAngle = dashAngle,
                viewWidth = width.toFloat(),
                viewHeight = height.toFloat()
            )
            val linesOriginatingFromYAxis = getLinesOriginatingFromYAxis(
                dashWidth = dashWidth,
                spaceBetweenDashes = spaceBetweenDashes,
                dashAngle = dashAngle,
                viewWidth = width.toFloat(),
                viewHeight = height.toFloat()
            )
            val dashDirection = getDashDirection(dashAngle)
            val allLinesToDraw =
                if (dashDirection.isHorizontal) linesOriginatingFromYAxis // If horizontal, only draw lines originating from Y axis
                else when (dashDirection) {
                    is DashDirection.LeftToRight -> linesOriginatingFromYAxis.reversed() + linesOriginatingFromXAxis
                    is DashDirection.RightToLeft -> linesOriginatingFromXAxis.reversed() + linesOriginatingFromYAxis
                    is DashDirection.Vertical -> linesOriginatingFromXAxis // If vertical, only draw lines originating from X axis
                }

            // Loop through coordinate list and draw lines
            for ((index, curCoords) in allLinesToDraw.withIndex()) {
                val startPoint = curCoords.startPoint
                val endPoint = curCoords.endPoint

                if (index % 3 == 0) {
//                    val paintColor = paintColorChooser.getPaintColor(index, allLinesToDraw.size)
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint)
                } else if (index % 3 == 1) {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint2)
                } else {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint3)
                }
            }



        }
    }

//    var paintColorChooser = object : Thingy {
//        override fun getPaintColor(curIndex: Int, numDashes: Int): Paint {
//            return dashPaint
//        }
//    }
//
//    interface Thingy {
//        fun getPaintColor(curIndex: Int, numDashes: Int): Paint
//    }
//
//    private fun getDashColor(): Paint {
//        TODO("Not yet implemented")
//    }

    // todo write unit tests
    private fun getLinesOriginatingFromXAxis(
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewWidth: Float,
        viewHeight: Float
    ): List<LineCoordinates> {

        // Check if horizontal config. If so, no lines drawn from x axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection.isHorizontal) return emptyList() // If dashes are horizontal (0 or 180 degrees) then no lines will originate from the x axis

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
        startPoints.add(Pair(curXPosition, startYPosition)) // Add one more line to ensure the view is fully covered by lines

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get line coordinates for dashes
        val endPointXTranslation = calculateEndPointXTranslation(dashAngle, viewHeight)
        val lineCoordinates = startPoints.map {
            LineCoordinates(
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
        val elongatedLineCoordinates = elongateDashesOriginatingFromXAxis(
            initialPositions = lineCoordinates,
            dashAngle = dashAngle,
            dashWidth = dashWidth
        )

        return elongatedLineCoordinates
    }

    private fun getLinesOriginatingFromYAxis(
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewWidth: Float,
        viewHeight: Float
    ): List<LineCoordinates> {

        // Check if vertical config. If so, no lines should be drawn from the y axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection is DashDirection.Vertical) return emptyList()

        // Calculate start points
        val startPositions = mutableListOf<Pair<Float, Float>>()
        val startXPosition = if (dashDirection is DashDirection.LeftToRight) VIEW_LEFT else viewWidth

        var curYPosition = viewHeight

        val dashVerticalOffset = calculateVerticalOffset(dashAngle, dashWidth)
        val spaceVerticalOffset = calculateVerticalOffset(dashAngle, spaceBetweenDashes)
        if (!dashDirection.isHorizontal) { // If lines will be drawn from the x axis, then skip drawing a line for the first y position which is equalt to the first x position
            curYPosition -= abs(dashVerticalOffset + spaceVerticalOffset)
        }
        while (curYPosition >= VIEW_TOP) {
            startPositions.add(Pair(startXPosition, curYPosition))
            curYPosition -= abs(dashVerticalOffset + spaceVerticalOffset)
        }
        startPositions.add(Pair(startXPosition, curYPosition)) // Add one more line to ensure the view is fully covered by lines

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get line coordinates for dashes
        val transition = calculateEndPointXTranslation(dashAngle, viewHeight)
        val lineCoordinates = startPositions.map {
            LineCoordinates(
                startPoint = Pair(
                    it.first,
                    it.second
                ),
                endPoint = generateEndPoint(
                    startPoint = it,
                    dashAngle = dashAngle,
                    endPointXTranslation = transition,
                    endPointYValue = it.second - viewHeight,// Subtract view height because each of these lines has a different start point Y value
                    viewWidth = viewWidth
                )
            )
        }

        // Translate start and end points so all 4 corners of dash are drawn outside of the view
        val elongatedLineCoordinates = elongateDashesOriginatingFromYAxis(
            initialPositions = lineCoordinates,
            dashAngle = dashAngle,
            dashWidth = dashWidth
        )

        return elongatedLineCoordinates
    }

    /**
     * Elongate the dashes enough so that all 4 corners of each dash are extended out of the view
     * canvas
     */
    private fun elongateDashesOriginatingFromXAxis(
        initialPositions: List<LineCoordinates>,
        dashAngle: Int,
        dashWidth: Float
    ): List<LineCoordinates> {
        val hypotenuseRadians = Math.toRadians((abs(90 - dashAngle).toDouble()))
        val translationHypotenuse = (dashWidth * tan((hypotenuseRadians))) / 2 // This is the amount the point must move
        val xTranslation = translationHypotenuse * sin(Math.toRadians((abs(90 - dashAngle).toDouble())))
        val yTranslation = translationHypotenuse * cos(Math.toRadians((abs(90 - dashAngle).toDouble())))

        return initialPositions.map {
            LineCoordinates(
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
     * Elongate the dashes enough so that all 4 corners of each dash are extended out of the view
     * canvas
     */
    private fun elongateDashesOriginatingFromYAxis(
        initialPositions: List<LineCoordinates>,
        dashAngle: Int,
        dashWidth: Float
    ): List<LineCoordinates> {
        val hypotenuseRadians = Math.toRadians(dashAngle.toDouble())
        val translationHypotenuse = (dashWidth * tan(hypotenuseRadians)) / 2 // This is the amount the point must move
        val xTranslation = abs(translationHypotenuse * cos(hypotenuseRadians)).toFloat()
        val yTranslation = abs(translationHypotenuse * sin(hypotenuseRadians)).toFloat()

        // Depending on the direction of lines the translation value needs to be negative
        val xTranslationModifier = when (getDashDirection(dashAngle)) {
            is DashDirection.LeftToRight -> -1
            is DashDirection.RightToLeft -> 1
            DashDirection.Vertical -> 0
        }

        return initialPositions.map {
            LineCoordinates(
                Pair(
                    it.startPoint.first + (xTranslation * xTranslationModifier),
                    it.startPoint.second + yTranslation
                ),
                it.endPoint // End point can remain unchanged for lines drawn from Y Axis. Their top corners wont show
            )
        }
    }

    private fun generateEndPoint(
        startPoint: Pair<Float, Float>,
        dashAngle: Int,
        endPointXTranslation: Float,
        endPointYValue: Float,
        viewWidth: Float
    ): Pair<Float, Float> {
        if (getDashDirection(dashAngle).isHorizontal)
            return Pair(viewWidth, startPoint.second)

        val calculatedEndPoint = Pair(startPoint.first + endPointXTranslation, endPointYValue)

//        val maxLength = lineLength(Pair(0f, 0f), Pair(viewWidth, viewHeight))
//        val closestPossibleEndPoint = calculateClosest(endPoint, startPoint, maxLength) todo get working

        return calculatedEndPoint
    }

    private fun calculateEndPointXTranslation(
        dashAngle: Int,
        viewHeight: Float
    ): Float {
        val dashAngleRadians = Math.toRadians(dashAngle.toDouble())
        val endPointXTranslation = viewHeight / tan(dashAngleRadians)
        return endPointXTranslation.toFloat()
    }

//    private fun calculateClosest(
//        endPoint: Pair<Float, Float>,
//        startPoint: Pair<Float, Float>,
//        maxLen: Float
//    ): Pair<Float, Float> {
//        val pointDist = lineLength(startPoint, endPoint)
//        if (pointDist > maxLen) {
//            val reductionRatio = maxLen / pointDist
//
//            val xDiff
//            return Pair(
//                endPoint.first * reductionRatio,
//                endPoint.second * reductionRatio
//            )
//        }
//        return endPoint
//    }
//
//    private fun lineLength(start: Pair<Float, Float>, end: Pair<Float, Float>): Float {
//        val xDiff = abs(start.first - end.first)
//        val yDiff = abs(start.second - end.second)
//        return sqrt(xDiff.toDouble().pow(2.0) + yDiff.toDouble().pow(2.0)).toFloat()
//    }

    /**
     * Gets the x translation required to sufficiently cover the corners of a line. Essentially this
     * function give info on how far a line should be extended so it doesn't show it's corners. This
     * is more important when dashes get thicker and resemble rectangles instead of lines
     */
    private fun calculateModifiedXTranslation(
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

    // todo write unit tests
    /**
     * Because the dashes can be drawn at an angle, the distance from one dash to the next should
     * actually be calculated using the hypotenuse. This method calculates that distance for
     * dashes and the spaces between dashes.
     */
    private fun calculateHorizontalOffset(angle: Int, width: Float): Float {
        val radians = Math.toRadians((90 - angle).toDouble())
        return width / (cos(radians)).toFloat()
    }

    private fun calculateVerticalOffset(angle: Int, width: Float): Float {
        val complementaryAngle = Math.toRadians(abs(90 - angle).toDouble())
        val halfVerticalLengthDashCrossSection = width / (2 * sin(complementaryAngle))
        return halfVerticalLengthDashCrossSection.toFloat() * 2f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastWidth = w
        lastHeight = h
    }
}