package com.mackhartley.dashedview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.cos

class DashedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Todo change this into an open source lib. Add other abilities such as specifying the period (instead of it just being equal to view height.) Perhaps default period could be min(height, width).
    // Todo it would be nice to integrate rounded edges/ends

    // Instance state
    private var dashwidth = DEFAULT_WIDTH
    private var spaceBetweenDashes = DEFAULT_SPACE_BETWEEN_DASHES
    @ColorInt private var dashColor = DEFAULT_COLOR
    private var cornerRadius = DEFAULT_CORNER_RADIUS
    private var dashAngle = DEFAULT_DASH_ANGLE

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

            for ((index, floatVal) in getHorizPositions(width.toFloat(), dashwidth, spaceBetweenDashes).withIndex()) {

                val startX = floatVal
                val startY = viewBottom

                val endX = (floatVal + height)
                val endY = VIEW_TOP

                if (index % 2 == 0) {
                    canvas.drawLine(startX, startY, endX, endY, dashPaint)

                } else {
                    canvas.drawLine(startX, startY, endX, endY, dashPaint2)


                }
            }
        }
    }

    // todo write unit tests
    private fun getHorizPositions(width: Float, dashWidth: Float, spaceBetweenDashes: Float): List<Float> {
        val startXPositions = mutableListOf<Float>()
        var curXPosition = 0f
        while (curXPosition <= width) {
            startXPositions.add(curXPosition)
            curXPosition += (calculateHypotenuseLen(dashAngle, dashWidth) + (spaceBetweenDashes))
        }
        return startXPositions
    }

    // todo write unit tests
    /**
     * Because the dashes can be drawn at an angle, the distance from one dash to the next should actually be
     * calculated using the hypotenuse.
     */
    private fun calculateHypotenuseLen(angle: Int, dashWidth: Float): Float {
        val radians = Math.toRadians(angle.toDouble())
        return dashWidth / (cos(radians)).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastWidth = w
        lastHeight = h
    }
}