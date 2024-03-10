package com.example.myapplication

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList
import java.util.Queue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val resourceId = R.raw.x20
        //val recordQueue: Queue<Triple<Long, Int, ByteArray>> = LinkedList()
        val channelArrays: Array<IntArray> = Array(16) { IntArray(0) }
//        val channelQueues: List<Queue<Int>> = List(16) { LinkedList() }
        var stream: DataInputStream? = null
        try {
            stream = readBinaryFileFromResources(this, resourceId)
            while (true) {
                val record = readRecord(stream, channelArrays)
                if (record == null) {
                    break
                }



                val (unixTime, msTime, data) = record
                println("Unix Time: $unixTime, MS Time: $msTime, Data: $data")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream?.close()
        }

        // Process channel data queues
        for ((index, queue) in channelArrays.withIndex()) {
            println("Channel $index data: $queue")
        }
    }


    fun readRecord(stream: DataInputStream, channelArrays: Array<IntArray>): Triple<Long, Int, List<ByteArray>>? {
        try {
            while (true) {
                val startByte = stream.readByte().toInt() and 0xFF
                if (startByte != 0xD) {
                    continue
                }
                val recordType = stream.readByte().toInt() and 0xFF
                if (recordType != 0xA0) {
                    continue
                }
                val unixTime = stream.readInt().toLong()
                val msTime = stream.readShort().toInt() and 0xFFFF
                val recordLength = (stream.readByte().toInt() and 0xFF) or ((stream.readByte().toInt() and 0xFF) shl 8)
                val data = mutableListOf<ByteArray>()
                for (i in 0 until 16) {
                    val channelData = ByteArray(2)
                    stream.read(channelData)
                    data.add(channelData)
                    // Convert two bytes to integer and add to the corresponding queue
                    val channelValue = (channelData[1].toInt() and 0xFF) shl 8 or (channelData[0].toInt() and 0xFF)
                    //channelQueues[i].add(channelValue)
                    channelArrays[i] = channelArrays[i] + channelValue
//                    channelArrays[i].plusAssign(channelValue)
                }
                val crc = stream.readShort().toInt()
                val stopByte = stream.readByte().toInt() and 0xFF
                if (stopByte != 0xA) {
                    throw IOException("Invalid stop byte")
                }
                return Triple(unixTime, msTime, data)
            }
        } catch (e: IOException) {
            return null
        }
    }

//    fun readRecord(stream: DataInputStream, channelQueues: List<Queue<Int>>): Triple<Long, Int, List<ByteArray>>? {
//        try {
//            while (true) {
//                val startByte = stream.readByte().toInt() and 0xFF
//                if (startByte != 0xD) {
//                    continue
//                }
//                val recordType = stream.readByte().toInt() and 0xFF
//                if (recordType != 0xA0) {
//                    continue
//                }
//                val unixTime = stream.readInt().toLong()
//                val msTime = stream.readShort().toInt() and 0xFFFF
//                val recordLength = (stream.readByte().toInt() and 0xFF) or ((stream.readByte().toInt() and 0xFF) shl 8)
//                val data = mutableListOf<ByteArray>()
//                for (i in 0 until 16) {
//                    val channelData = ByteArray(2)
//                    stream.read(channelData)
//                    data.add(channelData)
//                    // Convert two bytes to integer and add to the corresponding queue
//                    val channelValue = (channelData[1].toInt() and 0xFF) shl 8 or (channelData[0].toInt() and 0xFF)
//                    channelQueues[i].add(channelValue)
//                }
//                val crc = stream.readShort().toInt()
//                val stopByte = stream.readByte().toInt() and 0xFF
//                if (stopByte != 0xA) {
//                    throw IOException("Invalid stop byte")
//                }
//                return Triple(unixTime, msTime, data)
//            }
//        } catch (e: IOException) {
//            return null
//        }
//    }

    fun readBinaryFileFromResources(context: Context, resourceId: Int): DataInputStream {
        return DataInputStream(context.resources.openRawResource(resourceId))
    }


}