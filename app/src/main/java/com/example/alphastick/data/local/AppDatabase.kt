package com.example.alphastick.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PermissionLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun permissionLogDao(): PermissionLogDao
}
