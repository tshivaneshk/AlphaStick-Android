package com.example.alphastick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PermissionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PermissionLogEntity>)

    @Query("SELECT * FROM permission_logs WHERE packageName = :packageName")
    suspend fun getLogsForPackage(packageName: String): List<PermissionLogEntity>
    
    @Query("DELETE FROM permission_logs")
    suspend fun clearLogs()
}
