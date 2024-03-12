package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var realDataArrived = false
    private val data = mutableListOf<Int>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var channelsAdapter: ChannelsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataRepository = DataRepository(this)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(dataRepository, this)
        )[MainViewModel::class.java]

        initGraphsList()

        // Start a coroutine to load data from file into the queue
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.loadDataFromRepository()
        }

        startLiveData()
    }

    private fun initGraphsList() {
        channelsAdapter = ChannelsAdapter(mainViewModel.currentData, mainViewModel.mainCounter)
        binding.graphsList.layoutManager = LinearLayoutManager(this)
        binding.graphsList.adapter = channelsAdapter
        channelsAdapter.observeLiveData(this@MainActivity)
    }

    private fun startLiveData() {
        if (!realDataArrived) {
            realDataArrived = true
            mainViewModel.startLiveData()
        }
    }

}