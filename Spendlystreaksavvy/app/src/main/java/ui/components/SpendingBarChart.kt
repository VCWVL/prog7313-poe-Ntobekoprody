package com.example.spendly.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun SpendingBarChart(
    categoryLabels: List<String>,
    spendingValues: List<Float>,
    minGoals: List<Float>,
    maxGoals: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AndroidView(factory = { context ->
            BarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600 // give it a decent height
                )

                description.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.valueFormatter = IndexAxisValueFormatter(categoryLabels)
                legend.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.setDrawInside(false)
            }
        }, update = { chart ->

            val entriesActual = spendingValues.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            val entriesMin = minGoals.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            val entriesMax = maxGoals.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            val actualSet = BarDataSet(entriesActual, "Actual").apply {
                color = AndroidColor.parseColor("#4CAF50")
            }

            val minSet = BarDataSet(entriesMin, "Min Goal").apply {
                color = AndroidColor.parseColor("#03A9F4")
            }

            val maxSet = BarDataSet(entriesMax, "Max Goal").apply {
                color = AndroidColor.parseColor("#F44336")
            }

            val groupSpace = 0.2f
            val barSpace = 0.05f
            val barWidth = 0.25f

            val data = BarData(actualSet, minSet, maxSet)
            data.barWidth = barWidth
            chart.data = data

            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum =
                0f + data.getGroupWidth(groupSpace, barSpace) * categoryLabels.size
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()
        })
    }
}
