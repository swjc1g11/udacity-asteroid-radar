package com.udacity.asteroidradar.repositories

import android.util.Log
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.*
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.domain.asDatabaseAsteroids
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainAsteroids
import com.udacity.asteroidradar.domain.ImageOfTheDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class NasaRespository(val database: AsteroidsDatabase) {

    enum class FilterOptions {
        SEVEN_DAYS, TODAY
    }

    private val formattedNextSevenDays = getNextSevenDaysFormattedDates()
    private val nasaService = Network.nasaService

    private var asteroidTimeFrameFilter = MutableLiveData<FilterOptions>(FilterOptions.SEVEN_DAYS)
    private val _databaseAsteroids = asteroidTimeFrameFilter.switchMap { customFilter ->
        val datesToInclude : ArrayList<String> = when(customFilter) {
            FilterOptions.TODAY -> arrayListOf<String>(formattedNextSevenDays.get(0))
            FilterOptions.SEVEN_DAYS -> formattedNextSevenDays
        }
        database.asteroidDao.getAsteroidsByCloseApproachDates(*datesToInclude.toTypedArray())
    }
    val asteroids = Transformations.map(_databaseAsteroids) {
        it.asDomainAsteroids()
    }

    val imageOfTheDay = MutableLiveData<ImageOfTheDay>()
    val isConnectionError = MutableLiveData<Boolean>(false)

    suspend fun getImageOfTheDay() {
        withContext(Dispatchers.IO) {
            try {
                var networkImageOfTheDay = nasaService.getImageOfTheDayUrl()
                withContext (Dispatchers.Main) {
                    if (networkImageOfTheDay.media_type == NetworkImageOfTheDay.MEDIA_TYPE_IMAGE) {
                        imageOfTheDay.postValue(networkImageOfTheDay.asDomainModel())
                    }
                }
            } catch (e: Exception) {
                Log.i(TAG, e.message ?: "Failed to load image of the day url")
            }
        }
    }

    fun setAsteroidTimeFrameFilter(filterOption: FilterOptions) {
        asteroidTimeFrameFilter.value = filterOption
    }

    suspend fun refreshAsteroidsForNextSevenDays() {
        val startDataFormatted = formattedNextSevenDays.get(0)
        val endDateFormatted = formattedNextSevenDays.get(formattedNextSevenDays.size - 1)
        loadAsteroidsBetweenDatesFromNetwork(startDataFormatted, endDateFormatted)
    }

    private suspend fun loadAsteroidsBetweenDatesFromNetwork(startDateFormatted: String, endDateFormatted: String) {
        withContext(Dispatchers.IO) {
            try {
                val stringResult = nasaService.getNearEarthObjectsBetweenDates(startDateFormatted, endDateFormatted)
                val asteroidJSONObject = JSONObject(stringResult)
                val asteroids = parseAsteroidsJsonResult(asteroidJSONObject)
                database.asteroidDao.insertAll(*asteroids.asDatabaseAsteroids())
                isConnectionError.postValue(false)
            } catch (e: Exception) {
                isConnectionError.postValue(true)
            }
        }
    }

    companion object {
        const val TAG = "NasaRepository"
    }

}