package com.example.spendly.ui.components

import android.graphics.Color
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.spendly.data.Budget
import com.example.spendly.data.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun BudgetBarChart(
    budgets: List<Budget>,
    transactions: List<Transaction>,
    categories: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val entries = remember(budgets, transactions) {
        budgets.mapIndexed { index, budget ->
            val spent = transactions
                .filter { it.categoryId == budget.categoryId && it.type == "expense" }
                .sumOf { it.amount }
                .toFloat()

            BarEntry(
                index.toFloat(),
                floatArrayOf(
                    budget.minimum.toFloat(),
                    spent,
                    budget.maximum.toFloat()
                )
            )
        }
    }

    val categoryLabels = remember(budgets) {
        budgets.map { categories[it.categoryId] ?: "Unknown" }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = true
                setDrawGridBackground(false)
                setFitBars(true)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(categoryLabels)
                }

                val dataSet = BarDataSet(entries, "Budget vs Spent").apply {
                    stackLabels = arrayOf("Min Goal", "Spent", "Max Goal")
                    colors = listOf(
                        Color.rgb(76, 175, 80),  // Green (min)
                        Color.rgb(255, 152, 0),  // Orange (spent)
                        Color.rgb(244, 67, 54)   // Red (max)
                    )
                }

                data = BarData(dataSet).apply {
                    barWidth = 0.5f
                    setDrawValues(true)
                }

                invalidate()
            }
        },
        update = { chart ->
            val newDataSet = BarDataSet(entries, "Budget vs Spent").apply {
                stackLabels = arrayOf("Min Goal", "Spent", "Max Goal")
                colors = listOf(
                    Color.rgb(76, 175, 80),
                    Color.rgb(255, 152, 0),
                    Color.rgb(244, 67, 54)
                )
            }

            chart.data = BarData(newDataSet).apply {
                barWidth = 0.5f
                setDrawValues(true)
            }

            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}
