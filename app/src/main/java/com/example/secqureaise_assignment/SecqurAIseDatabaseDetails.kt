package com.example.secqureaise_assignment

import androidx.room.Entity

@Entity(tableName = "SecqurAIseDataDetails")
data class SecqurAIseDatabaseDetails(
    val captureCount: Int,
    val frequencyCount: Int,
    val connectivityStatus: String,
    val chargingStatus: String,
    val batteryPercentage: Int,
    val locationLongitude: String,
    val locationLatitude: String
)
