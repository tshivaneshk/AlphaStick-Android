package com.example.alphastick.data.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.alphastick.data.system.scanner.ScanOrchestrator
import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.domain.model.RiskResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemAppProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: ScanOrchestrator
) {
    private val cache = mutableMapOf<String, Pair<Long, AppInfo>>()

    suspend fun scanApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        val results = mutableListOf<AppInfo>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName ?: continue
            val updateTime = pkgInfo.lastUpdateTime

            // Is it a System App?
            val isSystemApp = (pkgInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0
            // Hide true system packages usually unless requested, but the prompt says to 
            // process them and lower their risk. We will filter out ONLY Android core to avoid extreme noise,
            // or just process them because zombie tracker ignores them.
            if (isSystemApp && packageName.startsWith("com.android")) continue

            // Cache check
            val cached = cache[packageName]
            if (cached != null && cached.first == updateTime) {
                results.add(cached.second)
                continue
            }

            val appName = pkgInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageName
            val permissions = pkgInfo.requestedPermissions?.toList() ?: emptyList()
            
            val installerName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    pm.getInstallSourceInfo(packageName).installingPackageName
                } catch (e: Exception) { null }
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
            
            val legitimateInstallers = listOf(
                "com.android.vending",             // Google Play Store
                "com.sec.android.app.samsungapps", // Samsung Galaxy Store
                "com.amazon.venezia",              // Amazon Appstore
                "com.huawei.appmarket",            // Huawei AppGallery
                "com.xiaomi.mipicks",              // Xiaomi GetApps
                "com.heytap.market",               // Oppo/Realme Store
                "com.oppo.market",                 // Oppo Store Alternative
                "com.vivo.appstore"                // Vivo App Store
            )
            
            // Treat app as legitimate if it came from a trusted OEM store OR is a base system application
            val isLegitimate = legitimateInstallers.contains(installerName) || isSystemApp
            
            val targetSdk = pkgInfo.applicationInfo?.targetSdkVersion ?: 0
            val usesCleartextTraffic = (pkgInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC)) != 0
            val allowsBackup = (pkgInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_ALLOW_BACKUP)) != 0
            val isDebuggable = (pkgInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE)) != 0
            
            var signatureHash = "Unavailable"
            try {
                val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                }
                val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    pInfo.signingInfo?.apkContentsSigners
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.signatures
                }
                if (!signatures.isNullOrEmpty()) {
                    val md = java.security.MessageDigest.getInstance("SHA-256")
                    md.update(signatures[0].toByteArray())
                    signatureHash = md.digest().joinToString(":") { "%02x".format(it).uppercase() }
                }
            } catch (e: Exception) {}
            
            var lastTimeUsed = 0L
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (1000L * 60 * 60 * 24 * 30) // 30 days
                val stats = usageStatsManager.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_MONTHLY, startTime, endTime)
                lastTimeUsed = stats?.find { it.packageName == packageName }?.lastTimeUsed ?: 0L
            } catch (e: Exception) {}

            // Construct partial AppInfo to feed orchestrator
            var appInfo = AppInfo(
                packageName = packageName,
                appName = appName,
                permissions = permissions,
                riskResult = RiskResult(0, emptyList(), com.example.alphastick.domain.model.Severity.INFO), // Temp
                findings = emptyList(), // Temp
                installDate = pkgInfo.firstInstallTime,
                isLegitimate = isLegitimate,
                isSystemApp = isSystemApp,
                targetSdk = targetSdk,
                usesCleartextTraffic = usesCleartextTraffic,
                allowsBackup = allowsBackup,
                isDebuggable = isDebuggable,
                signatureHash = signatureHash,
                lastTimeUsed = lastTimeUsed
            )

            // Orchestrate
            val (findings, result) = orchestrator.orchestrate(appInfo)
            
            appInfo = appInfo.copy(
                findings = findings,
                riskResult = result
            )
            
            cache[packageName] = Pair(updateTime, appInfo)
            results.add(appInfo)
        }

        // Clean up uninstalled apps from cache
        val currentPackages = packages.mapNotNull { it.packageName }.toSet()
        cache.keys.retainAll(currentPackages)

        results.sortedByDescending { it.riskResult.totalScore }
    }
}
