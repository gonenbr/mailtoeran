package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val channelsData: Array<IntArray> = Array(16) { IntArray(0) }
        val dataRepository = DataRepository(channelsData)

        val stream = readBinaryFileFromResources(this@MainActivity, R.raw.x20)

        // Start a coroutine to load data from file into the queue
        CoroutineScope(Dispatchers.IO).launch {
            dataRepository.loadDataFromFile(stream)
        }

        dataRepository.parsedData.observe(this) {
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            tvStatus.text = "Data is ready!"
        }

    }

    private fun readBinaryFileFromResources(context: Context, resourceId: Int): DataInputStream {
        return DataInputStream(context.resources.openRawResource(resourceId))
    }

}