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

    // Todo would be nice to be able to set an outline stroke with a custom width and color
    //todo dash offset
    //todo steps could be padded with an extra start and end step. This would avoid missing lines
    // 0 degree angle needs to work

    // Instance state
    private var dashwidth = DEFAULT_WIDTH
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
            strokeWidth = dashwidth
            isAntiAlias = true
        }
    }

    private val dashPaint2 by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = Color.GREEN
            strokeWidth = dashwidth
            isAntiAlias = true
        }
    }

    private val dashPaint3 by lazy {
        Paint().apply {
            strokeCap = Paint.Cap.BUTT
            color = Color.BLUE
            strokeWidth = dashwidth
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
        dashwidth = attrRefs.getDimension(R.styleable.DashedView_dashWidth, DEFAULT_WIDTH)
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

            val linesOriginatingFromXAxis = getLinesOriginatingFromXAxis(width.toFloat(), dashwidth, spaceBetweenDashes, dashAngle, height.toFloat())
            val linesOriginatingFromYAxis = getLinesOriginatingFromYAxis(dashAngle, height.toFloat(), width.toFloat())

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

    // todo write unit tests
    private fun getLinesOriginatingFromXAxis(
        width: Float,
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewHeight: Float
    ): List<LineCoordinates> {

        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection.isHorizontal) return emptyList() // If dashes are horizontal (0 or 180 degrees) then no lines will originate from this axis

        // Get start points
        val startPositions = mutableListOf<Pair<Float, Float>>()
        var curXPosition = 0f
        while (curXPosition <= width) {
            startPositions.add(Pair(curXPosition, viewHeight))
            curXPosition += (calculateHypotenuseLen(dashAngle, dashWidth) + calculateHypotenuseLen(dashAngle, spaceBetweenDashes))
        }

        // Get endpoints and group with appropriate start points
        val endPointXTrans = getEndPointXTranslation(dashAngle, viewHeight)
        val z = startPositions.map {
            LineCoordinates(
                startPoint = it,
                endPoint = Pair(
                    it.first + endPointXTrans,
                    0f // todo make it clear this is the top
                )
            )
        }

        // Translate start point butts
        val hypotRadians = Math.toRadians((abs(90 - dashAngle).toDouble()))
        val translationHypot = (dashWidth * tan((hypotRadians))) / 2
        val xTranslation = translationHypot * sin(Math.toRadians((abs(90 - dashAngle).toDouble())))
        val yTranslation = translationHypot * cos(Math.toRadians((abs(90 - dashAngle).toDouble())))
        val ret = startPositions.map {
            Pair(it.first - xTranslation.toFloat(), it.second - yTranslation.toFloat())
        }
        val z2 = z.map {
            LineCoordinates(
                Pair(
                    it.startPoint.first + getXTransBasedOn90(xTranslation, dashAngle),
                    it.startPoint.second + yTranslation.toFloat()
                ),
                Pair(
                    it.endPoint.first,
                    it.endPoint.second
                )
            )
        }

        return z2
    }

    private fun getLinesOriginatingFromYAxis(
        dashAngle: Int,
        viewHeight: Float,
        viewWidth: Float
    ): List<LineCoordinates> {

        val dashDirection = getDashDirection(dashAngle)
        if (dashDirection is DashDirection.Vertical) return emptyList() // If all dashes are vertical (90 degrees) then no dashes will originate from the y axis

        val startYPositions = mutableListOf<Float>()
        var curYPosition = viewHeight // This is the bottom left corner of the view

        curYPosition -= (calculateVerticalOffset(dashwidth, dashAngle) + calculateVerticalOffset(spaceBetweenDashes, dashAngle)) // The y = 0 position already has a dash drawn from the horizontal algo


        while (curYPosition >= 0) {
            startYPositions.add(curYPosition) // todo ensure this never becomes an infinite loop. Same for all other loops
            curYPosition -= abs(calculateVerticalOffset(dashwidth, dashAngle) + calculateVerticalOffset(spaceBetweenDashes, dashAngle)) // The y = 0 position already has a dash drawn from the horizontal algo
//            if (xx == true) xx = false
//            else break
//            break
        }


        // These are the same for all lines drawn from the Y axis
        val endYPos = 0f
        val startXPos = if (dashDirection is DashDirection.LeftToRight) 0f else viewWidth

        return startYPositions.map {
            val xTranslation = getEndPointXTranslation(dashAngle, it)
            val endPointX =
                if (dashDirection is DashDirection.LeftToRight) startXPos + xTranslation
                else startXPos + xTranslation
            LineCoordinates(
                startPoint = Pair(
                    startXPos,
                    it
                ),
                endPoint = Pair(
                    endPointX,
                    endYPos
                )
            )
        }
    }

    private fun calculateVerticalOffset(width: Float, angle: Int): Float {
        val complementaryAngle = Math.toRadians(abs(90 - angle).toDouble())
        val halfVerticalLengthDashCrossSection = width / (2 * sin(complementaryAngle))
        return halfVerticalLengthDashCrossSection.toFloat() * 2f
    }

    private fun getXTransBasedOn90(xTranslation: Double, angle: Int): Float {
        val dashDirection = getDashDirection(angle)
        val adjustedXTranslation: Double = when (dashDirection) {
            is DashDirection.LeftToRight -> xTranslation * -1
            is DashDirection.RightToLeft -> xTranslation
            is DashDirection.Vertical -> 0.0
        }
        return adjustedXTranslation.toFloat()
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