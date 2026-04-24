package com.example.alphastick.data.system.scanner

import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.model.RiskFactor
import com.example.alphastick.domain.model.RiskResult
import com.example.alphastick.domain.model.SecurityFinding
import com.example.alphastick.domain.model.Severity
import javax.inject.Inject

class ScanOrchestrator @Inject constructor() {
    
    private val scanners = listOf(
        PermissionScanner(),
        ManifestScanner(),
        UsageScanner(),
        InstallerScanner()
    )

    suspend fun orchestrate(app: AppInfo): Pair<List<SecurityFinding>, RiskResult> {
        val allFindings = mutableListOf<SecurityFinding>()
        for (scanner in scanners) {
            allFindings.addAll(scanner.scan(app))
        }

        val riskFactors = allFindings.map { finding ->
            val scoreImpact = when (finding.severity) {
                Severity.CRITICAL -> 10
                Severity.HIGH -> 5
                Severity.MEDIUM -> 3
                Severity.LOW -> 1
                Severity.INFO -> 0
            }
            RiskFactor(
                name = finding.title,
                scoreImpact = scoreImpact,
                reason = finding.reason
            )
        }

        val totalScore = riskFactors.sumOf { it.scoreImpact }
        
        val overallSeverity = when {
            totalScore >= 15 -> Severity.CRITICAL
            totalScore >= 10 -> Severity.HIGH
            totalScore >= 5 -> Severity.MEDIUM
            totalScore > 0 -> Severity.LOW
            else -> Severity.INFO
        }

        val riskResult = RiskResult(
            totalScore = totalScore,
            factors = riskFactors,
            severity = overallSeverity
        )

        return Pair(allFindings, riskResult)
    }
}
