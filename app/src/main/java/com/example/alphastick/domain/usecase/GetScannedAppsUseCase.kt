package com.example.alphastick.domain.usecase

import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.repository.AppRepository
import javax.inject.Inject

class GetScannedAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): List<AppInfo> {
        return repository.getScannedApps()
    }
}
