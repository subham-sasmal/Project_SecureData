package com.example.secqureaise_assignment

data class SecqureaiseDetails(
    val timeStamp: String,
    val dateStamp: String,
    val captureCount: Int,
    val frequencyCount: Int,
    val connectivityStatus: String,
    val chargingStatus: String,
    val batteryPercentage: Int,
    val locationLongitude: String,
    val locationLatitude: String
)
