package com.example.secqureaise_assignment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SecqurAIseDatabaseDetails::class], version = 1)
abstract class SecqurAIseDB: RoomDatabase() {
    abstract fun secqurAisedao(): SecqureaiseDetailsDAO

    companion object {
        @Volatile
        private var INSTANCE: SecqurAIseDB? = null

        fun getDB(context: Context): SecqurAIseDB {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                                SecqurAIseDB::class.java,
                                "DataDB").build()
                }
            }

            return INSTANCE!!
        }
    }
}