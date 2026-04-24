package com.example.alphastick.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alphastick.domain.usecase.GetScannedAppsUseCase
import com.example.alphastick.presentation.state.MainState
import com.example.alphastick.presentation.state.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getScannedAppsUseCase: GetScannedAppsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    init {
        loadApps()
    }

    fun refreshApps() {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val apps = getScannedAppsUseCase()
                _state.value = _state.value.copy(
                    isLoading = false, 
                    apps = apps,
                    filteredApps = sortApps(apps, _state.value.sortOption)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage ?: "Error loading apps")
            }
        }
    }

    fun onSortOptionSelected(option: SortOption) {
        _state.value = _state.value.copy(
            sortOption = option,
            filteredApps = sortApps(_state.value.apps, option)
        )
    }

    private fun sortApps(apps: List<com.example.alphastick.domain.model.AppInfo>, option: SortOption): List<com.example.alphastick.domain.model.AppInfo> {
        return when (option) {
            SortOption.RISK_SCORE -> apps.sortedByDescending { it.riskResult.totalScore }
            SortOption.INSTALL_DATE -> apps.sortedByDescending { it.installDate }
        }
    }
}
