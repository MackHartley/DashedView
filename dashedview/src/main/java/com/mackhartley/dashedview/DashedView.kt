package com.mackhartley.dashedview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class DashedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Todo change this into an open source lib. Add other abilities such as specifying the period (instead of it just being equal to view height.) Perhaps default period could be min(height, width).
    // Todo it would be nice to integrate rounded edges/ends

    // Instance state
    private var density = DEFAULT_DENSITY
    private var dashwidth = DEFAULT_WIDTH
    private var cornerRadius = DEFAULT_CORNER_RADIUS
    @ColorInt
    private var dashColor = DEFAULT_COLOR

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
            strokeCap = Paint.Cap.ROUND
            color = dashColor
            strokeWidth = dashwidth
            isAntiAlias = true
        }
    }

    companion object {
        const val DEFAULT_DENSITY = 3 // Number of lines per period (Currently period is hardcoded to be equal to view height)
        const val DEFAULT_WIDTH = 4f // Width of dash
        const val DEFAULT_COLOR = Color.GRAY
        const val DEFAULT_CORNER_RADIUS = 0f
        const val VIEW_LEFT = 0f
        const val VIEW_TOP = 0f
    }

    init {
        val attrRefs = context.obtainStyledAttributes(attrs, R.styleable.DashedView)
        density = attrRefs.getInteger(R.styleable.DashedView_density, DEFAULT_DENSITY)
        dashwidth = attrRefs.getDimension(R.styleable.DashedView_dashWidth, DEFAULT_WIDTH)
        cornerRadius = attrRefs.getDimension(R.styleable.DashedView_cornerRadius, DEFAULT_CORNER_RADIUS)
        dashColor = attrRefs.getColor(R.styleable.DashedView_dashColor, DEFAULT_COLOR)
        attrRefs.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        if (canvas != null) {
            canvas.clipPath(roundedCornersClipPath)
            val viewBottom = height.toFloat()

            if (density > 1) { // Need to add dashes up left hand side
                for (i in height downTo 0 step (height / density)) {
                    if (i != height) { // Skip first index because it'll be handled in the next for loop
                        canvas.drawLine(
                            VIEW_LEFT, // startX
                            i.toFloat(), // startY
                            width.toFloat(), // stopX
                            (i - width).toFloat(), // stopY
                            dashPaint
                        )
                    }
                }
            }

            for (i in 0..width step (height / density)) {
                canvas.drawLine(i.toFloat(), viewBottom, (i + height).toFloat(), VIEW_TOP, dashPaint)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastWidth = w
        lastHeight = h
    }
}