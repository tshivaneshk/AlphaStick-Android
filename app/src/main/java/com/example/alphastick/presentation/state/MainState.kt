package com.example.alphastick.presentation.state

import com.example.alphastick.domain.model.AppInfo

enum class SortOption {
    RISK_SCORE, INSTALL_DATE
}

data class MainState(
    val isLoading: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val error: String = "",
    val sortOption: SortOption = SortOption.RISK_SCORE
)
