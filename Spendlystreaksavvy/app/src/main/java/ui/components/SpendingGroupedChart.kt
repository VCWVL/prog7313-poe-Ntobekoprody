package com.example.spendly.ui.components

import android.graphics.Color as GColor
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun SpendingGroupedChart(
    labels: List<String>,
    actuals: List<Float>,
    mins: List<Float>,
    maxs: List<Float>,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) GColor.BLACK else GColor.WHITE
    val axisTextColor = if (isDark) GColor.WHITE else GColor.BLACK
    val gridLineColor = if (isDark) GColor.DKGRAY else GColor.LTGRAY

    AndroidView(factory = { context ->
        BarChart(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, 600)
            setDrawGridBackground(false)
            setBackgroundColor(backgroundColor)
            setScaleEnabled(false)
            description.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                textColor = axisTextColor
                gridColor = gridLineColor
            }

            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -25f
                textColor = axisTextColor
            }

            legend.apply {
                isEnabled = true
                textColor = axisTextColor
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }, update = { chart ->
        val barWidth = 0.2f
        val barSpace = 0.05f
        val groupSpace = 0.3f
        val groupCount = labels.size
        val startX = 0f

        val entriesActuals = actuals.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val entriesMins = mins.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val entriesMaxs = maxs.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

        val setActuals = BarDataSet(entriesActuals, "Spent").apply {
            color = GColor.rgb(33, 150, 243)
            valueTextColor = axisTextColor
        }

        val setMins = BarDataSet(entriesMins, "Min Goal").apply {
            color = GColor.rgb(76, 175, 80)
            valueTextColor = axisTextColor
        }

        val setMaxs = BarDataSet(entriesMaxs, "Max Goal").apply {
            color = GColor.rgb(244, 67, 54)
            valueTextColor = axisTextColor
        }

        val barData = BarData(setActuals, setMins, setMaxs)
        barData.barWidth = barWidth
        chart.data = barData

        val groupWidth = barData.getGroupWidth(groupSpace, barSpace)
        chart.xAxis.axisMinimum = startX
        chart.xAxis.axisMaximum = startX + groupCount * groupWidth
        chart.groupBars(startX, groupSpace, barSpace)

        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in labels.indices) labels[index] else ""
            }
        }

        chart.invalidate()
    }, modifier = modifier)
}
