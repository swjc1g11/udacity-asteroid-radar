package com.udacity.asteroidradar.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.repositories.NasaRespository
import retrofit2.HttpException

class RefreshAsteroidsWorker(val appContext: Context, val params: WorkerParameters): CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME ="RefreshAsteroidsWorker"
    }

    override suspend fun doWork(): Result {
        val database = AsteroidsDatabase.getDatabase(appContext)
        val repository = NasaRespository(database)
        return try {
            repository.refreshAsteroidsForNextSevenDays()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}