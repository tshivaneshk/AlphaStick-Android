package com.example.alphastick.domain.model

enum class Confidence {
    LOW, MEDIUM, HIGH
}

enum class Severity {
    INFO, LOW, MEDIUM, HIGH, CRITICAL
}

data class RiskFactor(
    val name: String,
    val scoreImpact: Int,
    val reason: String
)

data class RiskResult(
    val totalScore: Int,
    val factors: List<RiskFactor>,
    val severity: Severity
)

data class SecurityFinding(
    val id: String,
    val title: String,
    val description: String,
    val severity: Severity,
    val confidence: Confidence,
    val reason: String,
    val mitigation: String
)
