package com.example.myapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.DataInputStream
import java.io.IOException

class DataRepository(private val context: Context) {

    private val _parsedData = MutableLiveData<Array<IntArray>>()
    private val channelArrays = Array(16) { IntArray(0) }

    val parsedData: LiveData<Array<IntArray>>
        get() = _parsedData


    fun loadDataFromFile() {
        val stream = getStream()

        try {
            while (true) {
                readRecord(stream, channelArrays)
                _parsedData.postValue(channelArrays)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            stream.close()
        }
    }

    private fun getStream(): DataInputStream {
        return readBinaryFileFromResources(context, R.raw.x20)
    }

    private fun readBinaryFileFromResources(context: Context, resourceId: Int): DataInputStream {
        return DataInputStream(context.resources.openRawResource(resourceId))
    }

    private fun readRecord(
        stream: DataInputStream,
        channelArrays: Array<IntArray>
    ): Array<IntArray>? {

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
                val unixTime = (stream.readByte().toInt() and 0xFF) or
                        ((stream.readByte().toInt() and 0xFF) shl 8) or
                        ((stream.readByte().toInt() and 0xFF) shl 16) or
                        ((stream.readByte().toInt() and 0xFF) shl 24)
                val msTime = stream.readShort().toInt() and 0xFFFF
                val recordLength = (stream.readByte().toInt() and 0xFF) or
                        ((stream.readByte().toInt() and 0xFF) shl 8)
                val packetIndex = stream.readShort().toInt() and 0xFFFF
                val channelMapping = stream.readShort().toInt() and 0xFFFF
                val samplingRate = stream.readShort().toInt() and 0xFFFF
                val downSamplingFactor = stream.readByte().toInt() and 0xFF

                // Read data section
                val bytesToRead =
                    recordLength - 7 // Subtracting the size of bytes before the actual data
                val data = ByteArray(bytesToRead)
                stream.readFully(data)

                // Read CRC and stop byte
                val crc = stream.readShort().toInt() and 0xFF
                val stopByte = stream.readByte().toInt() and 0xFF
                if (stopByte != 0xA) {
                    throw IOException("Invalid stop byte")
                }

                // Organize data for channels
                var dataIndex = 0 // Index to track the current byte in the data array
                for (channelIndex in 0 until 16) {
                    if ((channelMapping shr channelIndex) and 0x01 == 1) {
                        val startByteIndex = dataIndex * 2
                        val channelData = (data[startByteIndex + 1].toInt() and 0xFF) shl 8 or
                                (data[startByteIndex].toInt() and 0xFF)
                        channelArrays[channelIndex] = channelArrays[channelIndex] + channelData
                        dataIndex++
                    }
                }

                return channelArrays
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}