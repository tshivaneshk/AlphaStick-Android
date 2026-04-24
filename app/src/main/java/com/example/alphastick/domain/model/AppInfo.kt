package com.example.alphastick.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val permissions: List<String>,
    val riskResult: RiskResult,
    val findings: List<SecurityFinding>,
    val installDate: Long,
    val isLegitimate: Boolean,
    val isSystemApp: Boolean,
    val targetSdk: Int,
    val usesCleartextTraffic: Boolean,
    val allowsBackup: Boolean,
    val isDebuggable: Boolean,
    val signatureHash: String,
    val lastTimeUsed: Long
)
