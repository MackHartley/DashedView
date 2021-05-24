package com.mackhartley.dashedviewexample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mackhartley.dashedview.DashedView
import com.mackhartley.dashedview.DashColorGenerator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Gives an example of a gradient
        val misc1 = findViewById<DashedView>(R.id.misc_example_1)
        misc1.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    val alphaValue = 255 * ((curIndex + 1).toFloat() / (numDashes + 1).toFloat())
                    val gradientAppliedColor = Color.argb(alphaValue.toInt(), 255, 0, 255)
                    return gradientAppliedColor
                }
            }
        )

        // Gives an example of a gradient
        val misc2 = findViewById<DashedView>(R.id.misc_example_2)
        misc2.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    val color = ContextCompat.getColor(applicationContext, R.color.design_default_color_secondary)

                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    val alphaValue = 255 * ((curIndex + 1).toFloat() / (numDashes + 1).toFloat())
                    val gradientAppliedColor = Color.argb(alphaValue.toInt(), red, green, blue)
                    return gradientAppliedColor
                }
            }
        )

        // Gives an example of a gradient
        val misc3 = findViewById<DashedView>(R.id.misc_example_3)
        misc3.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    val alphaValue = 255 * ((curIndex + 1).toFloat() / (numDashes + 1).toFloat())
                    val gradientAppliedColor = Color.argb(alphaValue.toInt(), 0, 0, 0)
                    return gradientAppliedColor
                }
            }
        )

        // Gives an example of an alternating color pallet
        val misc4 = findViewById<DashedView>(R.id.misc_example_4)
        misc4.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    return when {
                        curIndex % 2 == 0 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.barber1
                        )
                        else -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.barber2
                        )
                    }
                }
            }
        )

        // Gives an example of an alternating color pallet
        val misc5 = findViewById<DashedView>(R.id.misc_example_5)
        misc5.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    return when {
                        curIndex % 5 == 0 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.rainbow1
                        )
                        curIndex % 5 == 1 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.rainbow2
                        )
                        curIndex % 5 == 2 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.rainbow3
                        )
                        curIndex % 5 == 3 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.rainbow4
                        )
                        else -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.rainbow5
                        )
                    }
                }
            }
        )
    }
}