package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {

    @Query("select * FROM asteroids WHERE closeApproachDate IN (:formattedDates) ORDER BY closeApproachDate ASC")
    fun getAsteroidsByCloseApproachDates(vararg formattedDates : String): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg videos: DatabaseAsteroid)
}

@Database(entities = [DatabaseAsteroid::class], version = 1, exportSchema = false)
abstract class AsteroidsDatabase : RoomDatabase() {

    abstract val asteroidDao: AsteroidDao

    companion object {
        @Volatile
        private lateinit var INSTANCE : AsteroidsDatabase

        fun getDatabase(context: Context) : AsteroidsDatabase {
            synchronized(AsteroidsDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AsteroidsDatabase::class.java,
                            "asteroids").build()
                }
            }
            return INSTANCE
        }
    }
}