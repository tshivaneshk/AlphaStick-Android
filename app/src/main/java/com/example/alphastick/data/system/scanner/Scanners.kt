package com.example.alphastick.data.system.scanner

import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.model.Confidence
import com.example.alphastick.domain.model.SecurityFinding
import com.example.alphastick.domain.model.Severity
import java.util.UUID

class PermissionScanner : AppScanner {
    override suspend fun scan(app: AppInfo): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()
        val p = app.permissions
        
        val hasCamera = p.any { it.contains("CAMERA") }
        val hasMic = p.any { it.contains("RECORD_AUDIO") }
        val hasLocation = p.any { it.contains("ACCESS_FINE_LOCATION") || it.contains("ACCESS_COARSE_LOCATION") }
        val hasContacts = p.any { it.contains("READ_CONTACTS") || it.contains("WRITE_CONTACTS") }
        val hasSms = p.any { it.contains("READ_SMS") || it.contains("SEND_SMS") }
        val hasInternet = p.any { it.contains("INTERNET") }

        val defaultConfidence = if (app.isSystemApp || app.isLegitimate) Confidence.MEDIUM else Confidence.HIGH

        if (hasCamera) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Camera Access",
                description = "App requests access to the device camera.",
                severity = Severity.HIGH,
                confidence = defaultConfidence,
                reason = "Camera permissions allow background image capture if maliciously manipulated.",
                mitigation = "Revoke access if app does not need photo capabilities."
            ))
        }
        
        if (hasMic) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Microphone Access",
                description = "App requests access to record audio.",
                severity = Severity.HIGH,
                confidence = defaultConfidence,
                reason = "Microphone permissions allow ambient room audio recording.",
                mitigation = "Revoke access if voice features are unused."
            ))
        }
        
        if (hasSms) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "SMS Access",
                description = "App requests access to read or send SMS.",
                severity = Severity.CRITICAL,
                confidence = defaultConfidence,
                reason = "SMS permissions are highly abused for OTP interception.",
                mitigation = "Immediately revoke unless it is a primary messaging client."
            ))
        }

        if (hasCamera && hasInternet) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Camera + Internet Synergy",
                description = "App can capture media and exfiltrate it online.",
                severity = Severity.HIGH,
                confidence = defaultConfidence,
                reason = "This combination allows remote spying.",
                mitigation = "Ensure the app genuinely requires both to function."
            ))
        }
        
        return findings
    }
}

class ManifestScanner : AppScanner {
    override suspend fun scan(app: AppInfo): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()
        val defaultConfidence = if (app.isSystemApp) Confidence.LOW else Confidence.HIGH

        if (app.isDebuggable) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Debuggable Flag Exported",
                description = "App is marked as debuggable in manifest.",
                severity = Severity.CRITICAL,
                confidence = defaultConfidence,
                reason = "Allows external tools to inject code and read app memory.",
                mitigation = "This is a developer error and should be reported to the app creator."
            ))
        }

        if (app.allowsBackup) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Allows ADB Backup",
                description = "App permits ADB data extraction.",
                severity = Severity.MEDIUM,
                confidence = defaultConfidence,
                reason = "Local SQLite databases and preferences can be extracted without root.",
                mitigation = "Use biometric lock or device encryption."
            ))
        }
        
        if (app.usesCleartextTraffic) {
             findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Cleartext Traffic Allowed",
                description = "App transmits data without HTTPS encryption.",
                severity = Severity.HIGH,
                confidence = defaultConfidence,
                reason = "Data transmitted over HTTP can be intercepted over public WiFi.",
                mitigation = "Avoid logging in or doing banking actions on this app when on untrusted networks."
            ))           
        }

        return findings
    }
}

class UsageScanner : AppScanner {
    override suspend fun scan(app: AppInfo): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()
        
        val daysSinceUsed = if (app.lastTimeUsed > 0L) (System.currentTimeMillis() - app.lastTimeUsed) / (1000L * 60 * 60 * 24) else 0L
        val hasSensitive = app.permissions.any { it.contains("LOCATION") || it.contains("CONTACT") || it.contains("CAMERA") || it.contains("RECORD_AUDIO") }
        
        if (daysSinceUsed > 14 && hasSensitive && !app.isSystemApp && app.lastTimeUsed != 0L) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Zombie Tracker Identified",
                description = "App has not been opened in 14 days but holds sensitive permissions.",
                severity = Severity.HIGH,
                confidence = Confidence.HIGH,
                reason = "Idle background apps with high access present an elevated threat profile.",
                mitigation = "Uninstall immediate to reduce attack surface."
            ))
        }
        return findings
    }
}

class InstallerScanner : AppScanner {
    override suspend fun scan(app: AppInfo): List<SecurityFinding> {
        val findings = mutableListOf<SecurityFinding>()
        if (!app.isLegitimate && !app.isSystemApp) {
            findings.add(SecurityFinding(
                id = UUID.randomUUID().toString(),
                title = "Sideloaded Unknown Origin",
                description = "App was not installed from Google Play.",
                severity = Severity.MEDIUM,
                confidence = Confidence.HIGH,
                reason = "Bypasses Google Play Protect safety mechanisms.",
                mitigation = "Verify the source checksum before usage."
            ))
        }
        return findings
    }
}
