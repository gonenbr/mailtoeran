package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.GraphViewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChannelsAdapter(private var data: LiveData<Array<IntArray>>,
                      private var mainCounter: LiveData<Int>) : RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder>(){

    // this data will hold the current data that needs to be displayed in the graph, not the whole data
    private var dataList: Array<IntArray> = emptyArray()
    private var counter = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChannelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GraphViewBinding.inflate(inflater, parent, false)

        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bindData(dataList[position], position, counter)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun observeLiveData(owner: LifecycleOwner) {
        data.observe(owner) { newData ->
            dataList = newData
            notifyDataSetChanged()
        }

        mainCounter.observe(owner) { mainCounter ->
            counter = mainCounter
        }
    }

    class ChannelViewHolder(private val binding: GraphViewBinding): RecyclerView.ViewHolder(binding.root) {
        private val FIXED_Y_MIN = 16000f
        private val FIXED_Y_MAX = 17000f

        private var data = IntArray(0){0}
        private var counter = 0

        init {
            setupGraph()
        }

        fun bindData(newData: IntArray, position: Int, mainCounter: Int) {
            // Add the new data to the existing array
            data = newData
            this.counter = mainCounter
            binding.channelNumber.text = "Channel No.$position counter = $counter"
            updateGraph()
        }

        private fun setupGraph() {
            // Customize x-axis
            val xAxis = binding.lineChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter()
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(true)
            xAxis.setDrawLabels(true)

            // Customize y-axis
            val yAxisLeft = binding.lineChart.axisLeft
            val yAxisRight = binding.lineChart.axisRight

            yAxisLeft.axisMinimum = FIXED_Y_MIN
            yAxisLeft.axisMaximum = FIXED_Y_MAX
            yAxisLeft.setDrawGridLines(true)

            yAxisRight.axisMinimum = FIXED_Y_MIN
            yAxisRight.axisMaximum = FIXED_Y_MAX
            yAxisRight.setDrawGridLines(true)


            binding.lineChart.description = null
            binding.lineChart.invalidate()
        }

        private fun updateGraph() {
            val entries = mutableListOf<Entry>()
            for (i in data.indices) {
                entries.add(Entry(i.toFloat(), data[i].toFloat()))
            }
            val dataSet = LineDataSet(entries, "Live Data")
            dataSet.setDrawIcons(false) // Disable icons for data points
            dataSet.setDrawValues(false) // Disable values for data points
            dataSet.setDrawCircles(false)
            dataSet.lineWidth = 2f // Set line width
            dataSet.color = Color.BLUE // Set line color

            binding.lineChart.data = LineData(dataSet)

            // Notify the chart that the data has changed
            binding.lineChart.data.notifyDataChanged()

            // Notify the chart that the view should be refreshed
            binding.lineChart.notifyDataSetChanged()
        }
    }
}