package com.example.alphastick.data.system.scanner

import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.model.SecurityFinding

interface AppScanner {
    suspend fun scan(app: AppInfo): List<SecurityFinding>
}
