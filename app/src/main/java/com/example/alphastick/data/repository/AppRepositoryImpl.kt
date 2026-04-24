package com.example.alphastick.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.example.alphastick.data.local.PermissionLogDao
import com.example.alphastick.data.local.PermissionLogEntity
import com.example.alphastick.data.system.SystemAppProvider
import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appProvider: SystemAppProvider,
    private val dao: PermissionLogDao,
    @ApplicationContext private val context: Context
) : AppRepository {

    override suspend fun scanAndSave(): List<AppInfo> {
        val apps = appProvider.scanApps()
        val logs = mutableListOf<PermissionLogEntity>()
        val currentTime = System.currentTimeMillis()
        val pm = context.packageManager

        apps.forEach { app ->
            app.permissions.forEach { perm ->
                val granted = pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED
                logs.add(
                    PermissionLogEntity(
                        packageName = app.packageName,
                        permission = perm,
                        granted = granted,
                        timestamp = currentTime
                    )
                )
            }
        }
        
        dao.clearLogs()
        dao.insertAll(logs)
        
        return apps
    }

    override suspend fun getScannedApps(): List<AppInfo> {
        return appProvider.scanApps()
    }
}
