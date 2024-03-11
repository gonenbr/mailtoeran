package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {
    private val MAX_VISIBLE_ENTRIES = 50
    private val FIXED_Y_MIN = 16000f
    private val FIXED_Y_MAX = 17000f

    private var counter = 0
    private var realDataArrived = false
    private lateinit var lineChart: LineChart
    private lateinit var seekBar: SeekBar
    private val data = mutableListOf<Int>()
    private val channelsData : Array<IntArray> = Array(16) { IntArray(0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lineChart = findViewById(R.id.lineChart)

        val channelsData: Array<IntArray> = Array(16) { IntArray(0) }
        val dataRepository = DataRepository(channelsData)

        val stream = readBinaryFileFromResources(this@MainActivity, R.raw.x20)

        // Start a coroutine to load data from file into the queue
        CoroutineScope(Dispatchers.IO).launch {
            dataRepository.loadDataFromFile(stream)
            dataRepository.loadDataFromFile(stream).collect { batch ->
                //updateGraph(batch[0].asList())
                updateDataSet(batch[0].toMutableList())
            }
        }

        dataRepository.parsedData.observe(this) {
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            tvStatus.text = "Data is ready!"
        }


        // Set up the line chart
        setUpLineChart()

        // Start updating the chart with new data
        startUpdatingChart()
    }

    private fun updateDataSet(batch: MutableList<Int>) {
        if (!realDataArrived) {
            val dataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
            dataSet.clear()
            counter = 0
            realDataArrived = true
        }

        data.addAll(batch)
        println("data size: ${data.size}")
        println("data: $data")
    }

    private fun readBinaryFileFromResources(context: Context, resourceId: Int): DataInputStream {
        return DataInputStream(context.resources.openRawResource(resourceId))
    }


    /////////////////   Graph Code ////////////////////////////////
    private fun setUpLineChart() {
        val entries = mutableListOf<Entry>()

        for (i in 0 until MAX_VISIBLE_ENTRIES) {
            entries.add(Entry(i.toFloat(), 0f)) // Initialize with zero values
        }

        counter = entries.size

        val dataSet = LineDataSet(entries, "Live Data")
        dataSet.setDrawIcons(false) // Disable icons for data points
        dataSet.setDrawValues(false) // Disable values for data points
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f // Set line width
        dataSet.color = Color.BLUE // Set line color

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_ENTRIES.toFloat())

        // Set minimum visible range
        lineChart.setVisibleXRangeMinimum(5f) // Adjust as needed

        // Customize x-axis
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.setDrawLabels(true)

        // Customize y-axis
        val yAxisLeft = lineChart.axisLeft
        val yAxisRight = lineChart.axisRight

        yAxisLeft.axisMinimum = FIXED_Y_MIN
        yAxisLeft.axisMaximum = FIXED_Y_MAX
        yAxisLeft.setDrawGridLines(true)

        yAxisRight.axisMinimum = FIXED_Y_MIN
        yAxisRight.axisMaximum = FIXED_Y_MAX
        yAxisRight.setDrawGridLines(true)

        lineChart.invalidate()
    }

    private fun updateGraph() {
        // Add the data to the graph
        // Assuming lineChart is your LineChart instance

        val index = counter// - MAX_VISIBLE_ENTRIES
        val newEntry = Entry(index.toFloat(), data[index].toFloat())

        // first time we need to init the data set
        if (lineChart.data == null) {
            val entries = mutableListOf<Entry>()
            entries.add(newEntry)
            val dataSet = LineDataSet(entries, "Live Data")
            dataSet.color = Color.BLUE
            lineChart.data = LineData(dataSet)
        } else {
            val dataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
            dataSet.color = Color.BLUE
            dataSet.addEntry(newEntry)

            if (dataSet.entryCount > MAX_VISIBLE_ENTRIES) {
                dataSet.removeEntry(0)
            }
        }


        println("adding new Entry: $newEntry")

//        for ((index, value) in data.withIndex()) {
//            val entry = Entry(index.toFloat(), value.toFloat())
//            println("adding new Entry: $entry")
//            lineChart.data.getDataSetByIndex(0).addEntry(entry)
//        }

//        dataSet.addEntry(newEntry)

        // Notify the chart that the data has changed
        lineChart.data.notifyDataChanged()

        // Notify the chart that the view should be refreshed
        lineChart.notifyDataSetChanged()

        // Move the view to the latest data
        lineChart.moveViewToX(data.size.toFloat())

        counter++
    }

    private fun updateChart() {
        val dataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
        val newEntry = Entry(counter.toFloat(), (Math.random() * 100).toFloat())
        println("new Entry: $newEntry")

        dataSet.addEntry(newEntry)
        if (dataSet.entryCount > MAX_VISIBLE_ENTRIES) {
            dataSet.removeEntry(0)
        }
        dataSet.notifyDataSetChanged()

        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()

        lineChart.moveViewToX(counter.toFloat())
        lineChart.invalidate()

        counter++
    }

    private fun startUpdatingChart() {
        fixedRateTimer("timer", true, 1000, 300) {
            runOnUiThread {
                println("updating chart")
//                    updateChart()
                if (data.isNotEmpty()) {
                    updateGraph()
                }
            }
        }
    }

}