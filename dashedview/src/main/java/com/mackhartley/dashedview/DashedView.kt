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
    // todo space between dashes needs to use the trig calculations

    // Instance state
    private var dashwidth = DEFAULT_WIDTH
    private var spaceBetweenDashes = DEFAULT_SPACE_BETWEEN_DASHES
    private var dashAngle = DEFAULT_DASH_ANGLE
    @ColorInt private var dashColor = DEFAULT_COLOR
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
        dashAngle = attrRefs.getInteger(R.styleable.DashedView_dashAngle, DEFAULT_DASH_ANGLE)
        attrRefs.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        if (canvas != null) {
            canvas.clipPath(roundedCornersClipPath)
            val viewBottom = height.toFloat()

//            if (density > 1) { // Need to add dashes up left hand side
//                for (i in height downTo 0 step (height / density)) {
//                    if (i != height) { // Skip first index because it'll be handled in the next for loop
//                        canvas.drawLine(
//                            VIEW_LEFT, // startX
//                            i.toFloat(), // startY
//                            width.toFloat(), // stopX
//                            (i - width).toFloat(), // stopY
//                            dashPaint
//                        )
//                    }
//                }
//            }

            for ((index, curCoords) in getStartPoints(width.toFloat(), dashwidth, spaceBetweenDashes, dashAngle, height.toFloat()).withIndex()) {
                val startPoint = curCoords.startPoint
                val endPoint = curCoords.endPoint

                if (index % 2 == 0) {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint)

                } else {
                    canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, dashPaint2)
                }
            }
        }
    }

    private fun getEndPointXTranslation(angle: Int, viewHeight: Float): Float {
        val radians = Math.toRadians(angle.toDouble())
        return viewHeight / tan(radians).toFloat() // todo check for divide by 0
    }

    // todo write unit tests
    private fun getStartPoints(
        width: Float,
        dashWidth: Float,
        spaceBetweenDashes: Float,
        dashAngle: Int,
        viewHeight: Float
    ): List<LineCoordinates> {

        // Get start points
        val startPositions = mutableListOf<Pair<Float, Float>>()
        var curXPosition = 0f
        while (curXPosition <= width) {
            startPositions.add(Pair(curXPosition, viewHeight))
            curXPosition += (calculateHypotenuseLen(dashAngle, dashWidth) + (spaceBetweenDashes))
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

    fun getXTransBasedOn90(xTranslation: Double, angle: Int): Float {
        val ret = if (angle > 90) xTranslation
        else xTranslation * -1
        return ret.toFloat()
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