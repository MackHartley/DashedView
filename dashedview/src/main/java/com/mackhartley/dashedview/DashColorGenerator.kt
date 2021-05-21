package com.mackhartley.dashedview

import androidx.annotation.ColorInt

interface DashColorGenerator {
    /**
     * This method allows you to set the paint color for a dash based on its index.
     * This could be used to set alternating colors (eg: blue, red, blue...) by using a modulus
     * operator. It could also be used to set dash colors in a gradient fashion (eg: lighter dashes
     * followed by darker dashes).
     *
     * @param curIndex - Current index of the dash that is being drawn
     * @param numDashes - The number of dashes that will be drawn. Note, sometimes the first or last
     * dash will be drawn out of the view depending on the [DashedView] configuration.
     *
     * @return The color value to display for this index. Make sure to return a [ColorInt] instead
     * of a normal [Int].
     * For example, instead of returning R.color.my_color, return ContextCompat.getColor(context, R.color.my_color)
     */
    @ColorInt
    fun getPaintColor(curIndex: Int, numDashes: Int): Int
}