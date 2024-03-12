package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Integer.min
import kotlin.concurrent.fixedRateTimer

class MainViewModel(
    private val dataRepository: DataRepository,
    private val activity: MainActivity
) : ViewModel() {

    private val _currentData: MutableLiveData<Array<IntArray>> = MutableLiveData(emptyArray())
    private val _mainCounter = MutableLiveData(0)

    val currentData: LiveData<Array<IntArray>>
        get() = _currentData

    val mainCounter: LiveData<Int>
        get() = _mainCounter

    fun loadDataFromRepository() {
        return dataRepository.loadDataFromFile()
    }

    fun startLiveData() {
        println("Starting timer")
        fixedRateTimer("timer", true, 1000, 500) {
            activity.runOnUiThread {
                println("updating main counter and current data")
                _mainCounter.postValue(mainCounter.value?.plus(1))
                _currentData.postValue(getDataForCounter())
            }
        }
    }

    private fun getDataForCounter(): Array<IntArray> {
        val result: MutableList<IntArray> = mutableListOf()

        dataRepository.parsedData.value?.forEachIndexed { index, array ->
            try {
                val startIdx = mainCounter.value ?: 0
                val endIndex = min(array.lastIndex, startIdx + 49)
                result.add(array.slice(startIdx..endIndex).toIntArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return result.toTypedArray()
    }
}