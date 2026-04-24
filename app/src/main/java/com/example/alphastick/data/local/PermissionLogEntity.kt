package com.example.alphastick.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permission_logs")
data class PermissionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val permission: String,
    val granted: Boolean,
    val timestamp: Long
)
