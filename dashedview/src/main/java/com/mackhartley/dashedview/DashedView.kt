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

    //todo fix color ordering
    // Todo dashes need to draw correctly when greater than 90 degrees
    // 0 degree angle needs to work
    //todo steps could be padded with an extra start and end step. This would avoid missing lines
    // Todo consolidate as many math calls as possible for efficiency sake

    // todo test performance

    //TOdo use VIEW_TOP and other helpers whereever it makes code more readable

    // Make git project:
        // Todo would be nice to be able to set an outline stroke with a custom width and color

    // Instance state
    private var dashWidth = DEFAULT_WIDTH
    private var spaceBetweenDashes = DEFAULT_SPACE_BETWEEN_DASHES
    private var dashAngle = DEFAULT_DASH_ANGLE
    @ColorInt
    private var dashColor = DEFAULT_COLOR
    private var cornerRadius = DEFAULT_CORNER_RADIUS

    private var lastWidth = width // Used for keeping track of view size
    private var lastHeight = height // Used for keeping track of view size

    private val roundedCornersClipPath by lazy { // This path is used to clip the progress background and drawable to the desired corner radius
        Path().apply {
            addRoundRect(
                0f,
                0f,
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
            color = Color.RED
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
        const val DEFAULT_COLOR = Color.GRAY
        const val DEFAULT_CORNER_RADIUS = 0f
        const val VIEW_LEFT = 0f
        const val VIEW_TOP = 0f
        const val DEFAULT_DASH_ANGLE = 45 // Measured in degrees. Min 0, max 180
    }

    init {
        val attrRefs = context.obtainStyledAttributes(attrs, R.styleable.DashedView)
        dashWidth = attrRefs.getDimension(R.styleable.DashedView_dashWidth, DEFAULT_WIDTH)
        spaceBetweenDashes = attrRefs.getDimension(R.styleable.DashedView_spaceBetweenDashes, DEFAULT_SPACE_BETWEEN_DASHES)
        dashColor = attrRefs.getColor(R.styleable.DashedView_dashColor, DEFAULT_COLOR)
        cornerRadius = attrRefs.getDimension(R.styleable.DashedView_cornerRadius, DEFAULT_CORNER_RADIUS)

        val requestedAngle = attrRefs.getInteger(R.styleable.DashedView_dashAngle, DEFAULT_DASH_ANGLE)
        dashAngle = if (requestedAngle < 0) 0 else if (requestedAngle > 180) 180 else requestedAngle

        attrRefs.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        if (canvas != null) {
            canvas.clipPath(roundedCornersClipPath)

            val linesOriginatingFromXAxis = getLinesOriginatingFromXAxis(width.toFloat(), dashWidth, spaceBetweenDashes, dashAngle, height.toFloat())
            val linesOriginatingFromYAxis = getLinesOriginatingFromYAxis(dashAngle, height.toFloat(), width.toFloat(), dashWidth, spaceBetweenDashes) //todo make these have same args list

            val allLinesToDraw = when (getDashDirection(dashAngle)) {
                is DashDirection.LeftToRight -> linesOriginatingFromYAxis.reversed() + linesOriginatingFromXAxis
                is DashDirection.RightToLeft -> linesOriginatingFromXAxis + linesOriginatingFromYAxis
                else -> linesOriginatingFromXAxis
            }

            for ((index, curCoords) in allLinesToDraw.withIndex()) {
                val startPoint = curCoords.startPoint
                val endPoint = curCoords.endPoint

                if (index % 3 == 0) {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint)
                } else if (index % 3 == 1) {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint2)
                } else {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint3)
                }
            }
        }
    }

    // todo write unit tests
    private fun getLinesOriginatingFromXAxis(
        width: Float,
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewHeight: Float
    ): List<LineCoordinates> {

        // Check if horizontal config. If so, no lines drawn from x axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection.isHorizontal) return emptyList() // If dashes are horizontal (0 or 180 degrees) then no lines will originate from the x axis

        // Calculate start points
        val startPoints = mutableListOf<Pair<Float, Float>>()
        val startYPosition = viewHeight

        if (dashDirection is DashDirection.LeftToRight) {
            var curXPosition = 0f
            while (curXPosition <= width) {
                startPoints.add(Pair(curXPosition, startYPosition))
                curXPosition += (calculateHypotenuseLen(dashAngle, dashWidth) + calculateHypotenuseLen(dashAngle, spaceBetweenDashes))
            }
        } else { // If the dashes are pointing from right to left, then start drawing dashes from the bottom right corner of the view
            var curXPosition = width
            while (curXPosition >= 0) {
                startPoints.add(Pair(curXPosition, startYPosition))
                curXPosition -= (calculateHypotenuseLen(dashAngle, dashWidth) + calculateHypotenuseLen(dashAngle, spaceBetweenDashes))
            }
        }

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get line coordinates for dashes
        val endPointXTranslation = getEndPointXTranslation(dashAngle, viewHeight)
        val lineCoordinates = startPoints.map {
            LineCoordinates(
                startPoint = it,
                endPoint = Pair(
                    it.first + endPointXTranslation,
                    VIEW_TOP
                )
            )
        }

        // Translate start and end points so all 4 corners of dash are drawn outside of the view
        val elongatedLineCoordinates = elongateDashesOriginatingFromXAxis(
            dashAngle,
            dashWidth,
            lineCoordinates
        )

        return elongatedLineCoordinates
    }

    private fun getLinesOriginatingFromYAxis(
        dashAngle: Int,
        viewHeight: Float,
        viewWidth: Float,
        dashWidth: Float,
        spaceBetweenDashes: Float
    ): List<LineCoordinates> {

        // Check if vertical config. If so, no lines should be drawn from the y axis
        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection is DashDirection.Vertical) return emptyList() // If all dashes are vertical (90 degrees) then no dashes will originate from the y axis

        // Calculate start points
        val startPositions = mutableListOf<Pair<Float, Float>>()
        val startXPosition = if (dashDirection is DashDirection.LeftToRight) 0f else viewWidth

        var curYPosition = viewHeight // This is the bottom left corner of the view
        curYPosition -= (calculateVerticalOffset(dashWidth, dashAngle) + calculateVerticalOffset(spaceBetweenDashes, dashAngle)) // The y = 0 position already has a dash drawn from the x axis
        while (curYPosition >= 0) {
            startPositions.add(Pair(startXPosition, curYPosition)) // todo ensure this never becomes an infinite loop. Same for all other loops
            curYPosition -= abs(calculateVerticalOffset(dashWidth, dashAngle) + calculateVerticalOffset(spaceBetweenDashes, dashAngle)) // The y = 0 position already has a dash drawn from the horizontal algo
        }

        // Calculate translation required to generate end point for a given start point
        // Apply translation to list of start points to get line coordinates for dashes
        val endPointXTranslation = getEndPointXTranslation(dashAngle, viewHeight)
        val lineCoordinates = startPositions.map {
            LineCoordinates(
                startPoint = Pair(
                    it.first,
                    it.second
                ),
                endPoint = Pair(
                    it.first + endPointXTranslation,
                    it.second - viewHeight // Subtract view height because each of these lines has a different start point Y value
                )
            )
        }

        // Translate start and end points so all 4 corners of dash are drawn outside of the view
        val elongatedLineCoordinates = elongateDashesOriginatingFromYAxis(
            dashAngle,
            dashWidth,
            lineCoordinates
        )

        return elongatedLineCoordinates
    }

    /**
     * Elongate the dashes enough so that all 4 corners of each dash are extended out of the view
     * canvas
     */
    private fun elongateDashesOriginatingFromXAxis(
        dashAngle: Int,
        dashWidth: Float,
        initialPositions: List<LineCoordinates>
    ): List<LineCoordinates> {
        val hypotRadians = Math.toRadians((abs(90 - dashAngle).toDouble()))
        val translationHypot = (dashWidth * tan((hypotRadians))) / 2
        val xTranslation = translationHypot * sin(Math.toRadians((abs(90 - dashAngle).toDouble())))
        val yTranslation = translationHypot * cos(Math.toRadians((abs(90 - dashAngle).toDouble())))
        // todo change to use translation modifier

        return initialPositions.map {
            LineCoordinates(
                Pair(
                    it.startPoint.first + getXTranslationToConcealLineCorners(xTranslation, dashAngle, false),
                    it.startPoint.second + yTranslation.toFloat()
                ),
                Pair(
                    it.endPoint.first + getXTranslationToConcealLineCorners(xTranslation, dashAngle, true),
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
        dashAngle: Int,
        dashWidth: Float,
        initialPositions: List<LineCoordinates>
    ): List<LineCoordinates> {

        val hypotRadians = Math.toRadians(dashAngle.toDouble())
        val translationHypot = (dashWidth * tan(hypotRadians)) / 2
        val xTranslation = abs(translationHypot * cos(hypotRadians)).toFloat()
        val yTranslation = abs(translationHypot * sin(hypotRadians)).toFloat()

        val translationModifier = when (getDashDirection(dashAngle)) {
            is DashDirection.LeftToRight -> -1
            is DashDirection.RightToLeft -> 1
            DashDirection.Vertical -> 0
        }

        return initialPositions.map {
            LineCoordinates(
                Pair(
                    it.startPoint.first + (xTranslation * translationModifier),
                    it.startPoint.second + yTranslation
                ),
                it.endPoint // Endpoint can remain unchanged for lines drawn from Y Axis. Their top corners wont show
            )
        }
    }

    private fun getDashDirection(dashAngle: Int): DashDirection {
        return when {
            dashAngle == 0 -> DashDirection.LeftToRight(true)
            dashAngle < 90 -> DashDirection.LeftToRight(false)
            dashAngle == 90 -> DashDirection.Vertical
            dashAngle < 180 -> DashDirection.RightToLeft(false)
            else -> DashDirection.RightToLeft(true)
        }
    }

    private fun getEndPointXTranslation(angle: Int, viewHeight: Float): Float {
        val radians = Math.toRadians(angle.toDouble())
        return viewHeight / tan(radians).toFloat() // todo check for divide by 0
    }

    private fun calculateVerticalOffset(width: Float, angle: Int): Float {
        val complementaryAngle = Math.toRadians(abs(90 - angle).toDouble())
        val halfVerticalLengthDashCrossSection = width / (2 * sin(complementaryAngle))
        return halfVerticalLengthDashCrossSection.toFloat() * 2f
    }

    /**
     * Gets the x translation required to sufficiently cover the corners of a line. Essentially this
     * function give info on how far a line should be extended so it doesn't show it's corners. This
     * is more important when dashes get thicker and resemble rectangles instead of lines
     */
    private fun getXTranslationToConcealLineCorners(
        xTranslation: Double,
        angle: Int,
        isTop: Boolean
    ): Float {
        val dashDirection = getDashDirection(angle)
        val adjustedXTranslation: Double = when (dashDirection) {
            is DashDirection.LeftToRight -> xTranslation * -1
            is DashDirection.RightToLeft -> xTranslation
            is DashDirection.Vertical -> 0.0
        }

        val z = if (isTop) adjustedXTranslation * -1 else adjustedXTranslation
        return z.toFloat()
    }

    // todo write unit tests
    /**
     * Because the dashes can be drawn at an angle, the distance from one dash to the next should actually be
     * calculated using the hypotenuse.
     */
    private fun calculateHypotenuseLen(angle: Int, dashWidth: Float): Float {
        val radians = Math.toRadians((90 - angle).toDouble())
        return dashWidth / (cos(radians)).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastWidth = w
        lastHeight = h
    }
}