package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.squareup.moshi.JsonReader
import com.udacity.asteroidradar.api.API_KEY
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.ImageOfTheDay
import com.udacity.asteroidradar.repositories.NasaRespository
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(val application: Application) : ViewModel() {

    /**
     * Repository set-up
     */
    private val database = AsteroidsDatabase.getDatabase(application.applicationContext)
    private val nasaRepository = NasaRespository(database)

    /**
     * Repository variables for the fragment to observe
     */
    val asteroids = nasaRepository.asteroids
    val imageOfTheDay = nasaRepository.imageOfTheDay
    val isConnectionError = nasaRepository.isConnectionError

    /***
     * ViewModel variables for the activity to update and observe
     */
    private val _asteroidsHaveBeenLoaded = MutableLiveData<Boolean>(false)
    val asteroidsHaveBeenLoaded : LiveData<Boolean>
        get() = _asteroidsHaveBeenLoaded

    private val _navigateToDetailScreenAsteroid = MutableLiveData<Asteroid?>()
    val navigateToDetailScreenAsteroid : LiveData<Asteroid?>
        get() = _navigateToDetailScreenAsteroid

    init {
        attemptNetworkCalls()
    }

    fun setNavigateToDetailScreenAsteroid(asteroid: Asteroid) {
        _navigateToDetailScreenAsteroid.value = asteroid
    }

    fun onNavigatedToDetailScreen() {
        _navigateToDetailScreenAsteroid.value = null
    }

    fun setAsteroidsHaveBeenLoaded(value: Boolean) {
        _asteroidsHaveBeenLoaded.value = value
    }

    fun loadAsteroidsForToday() {
        nasaRepository.setAsteroidTimeFrameFilter(NasaRespository.FilterOptions.TODAY)
    }

    fun loadAsteroidsForNextSevenDays() {
        nasaRepository.setAsteroidTimeFrameFilter(NasaRespository.FilterOptions.SEVEN_DAYS)
    }

    fun attemptNetworkCalls() {
        refreshImageOfTheDayUrl()
        refreshAsteroidsFromNetwork()
    }

    private fun refreshImageOfTheDayUrl() {
        viewModelScope.launch {
            nasaRepository.getImageOfTheDay()
        }
    }

    private fun refreshAsteroidsFromNetwork() {
        viewModelScope.launch {
            nasaRepository.refreshAsteroidsForNextSevenDays()
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}