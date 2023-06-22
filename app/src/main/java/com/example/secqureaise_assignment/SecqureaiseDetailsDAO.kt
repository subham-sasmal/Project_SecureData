package com.example.secqureaise_assignment

import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SecqureaiseDetailsDAO {
    @Insert
    suspend fun insertDetails(objectOfSecqurAIseDatabaseDetails: SecqurAIseDatabaseDetails)

    //@Query(value = "SELECT * FROM secquraisedatadetails")
    // suspend fun getDetails() : LiveData<List<SecqurAIseDatabaseDetails>>
}