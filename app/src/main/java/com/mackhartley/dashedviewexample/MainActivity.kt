package com.mackhartley.dashedviewexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mackhartley.dashedview.DashedView
import com.mackhartley.dashedview.DashColorGenerator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dashedView = findViewById<DashedView>(R.id.dashedview)
        dashedView.setDashColorGenerator(
            object : DashColorGenerator {
                override fun getPaintColor(curIndex: Int, numDashes: Int): Int {
                    return when {
                        curIndex % 3 == 0 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.design_default_color_error
                        )
                        curIndex % 3 == 1 -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.design_default_color_secondary
                        )
                        else -> ContextCompat.getColor(
                            this@MainActivity.applicationContext,
                            R.color.design_default_color_primary
                        )
                    }
                }

            }
        )
    }
}