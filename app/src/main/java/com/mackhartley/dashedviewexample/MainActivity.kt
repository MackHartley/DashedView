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

        val misc2 = findViewById<DashedView>(R.id.misc_example_2)
        misc2.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    val alphaValue = 255 * ((curIndex + 1).toFloat() / (numDashes + 1).toFloat())
                    val gradientAppliedColor = Color.argb(alphaValue.toInt(), 255, 0, 255)
                    return gradientAppliedColor
                }

            }
        )

        val misc3 = findViewById<DashedView>(R.id.misc_example_3)
        misc3.setDashColorGenerator(
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
    }
}