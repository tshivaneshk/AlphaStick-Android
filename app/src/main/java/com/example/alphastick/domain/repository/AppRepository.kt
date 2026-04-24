package com.example.alphastick.domain.repository

import com.example.alphastick.domain.model.AppInfo

interface AppRepository {
    suspend fun scanAndSave(): List<AppInfo>
    suspend fun getScannedApps(): List<AppInfo>
}
