package com.example.alphastick.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    packageName: String,
    onBackClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val app = state.apps.find { it.packageName == packageName }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(bgGradient)
    ) { padding ->
        if (app == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("App not found", color = MaterialTheme.colorScheme.onBackground)
            }
            return@Scaffold
        }

        val riskColor = when (app.riskResult.severity) {
            com.example.alphastick.domain.model.Severity.CRITICAL -> Color(0xFFFF4B4B)
            com.example.alphastick.domain.model.Severity.HIGH -> Color(0xFFFF4B4B)
            com.example.alphastick.domain.model.Severity.MEDIUM -> Color(0xFFFFAE42)
            com.example.alphastick.domain.model.Severity.LOW -> Color(0xFF81C784)
            com.example.alphastick.domain.model.Severity.INFO -> Color(0xFF4CAF50)
        }
        val riskName = app.riskResult.severity.name

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val initialLabel = app.appName.take(1).uppercase()
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Transparent, CircleShape)
                            .shadowAndBorder(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        var appIcon by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                        
                        LaunchedEffect(app.packageName) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                                    val bitmap = drawable.toBitmap(256, 256)
                                    appIcon = bitmap.asImageBitmap()
                                } catch (e: Exception) {}
                            }
                        }
                        
                        if (appIcon != null) {
                            androidx.compose.foundation.Image(
                                bitmap = appIcon!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))), CircleShape), contentAlignment = Alignment.Center) {
                                Text(initialLabel, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 42.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable {
                                    android.widget.Toast.makeText(
                                        localContext,
                                        if (app.isLegitimate) "Legitimate Source (Play Store)" else "Unknown Source / APK",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .background(
                                    if (app.isLegitimate) Color(0xFF4CAF50).copy(alpha=0.1f) else Color(0xFFFF4B4B).copy(alpha=0.1f),
                                    CircleShape
                                )
                                .border(1.dp, if (app.isLegitimate) Color(0xFF4CAF50).copy(alpha=0.5f) else Color(0xFFFF4B4B).copy(alpha=0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (app.isLegitimate) "L" else "U",
                                color = if (app.isLegitimate) Color(0xFF4CAF50) else Color(0xFFFF4B4B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(app.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Risk Gauge Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadowAndBorder(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Risk Assessment", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(riskName, color = riskColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                            CircularProgressIndicator(progress = app.riskResult.totalScore / 30f, modifier = Modifier.fillMaxSize(), color = riskColor, strokeWidth = 6.dp, trackColor = riskColor.copy(alpha = 0.2f))
                            Text(app.riskResult.totalScore.toString(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            // Findings / "Why is this flagged?"
            item {
                Text("Security Findings Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (app.findings.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadowAndBorder(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No internal threats or suspicious telemetry detected.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        app.findings.forEach { finding ->
                            Card(
                                modifier = Modifier.fillMaxWidth().shadowAndBorder(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = riskColor, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(finding.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(finding.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Why is this flagged?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(finding.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Confidence Level:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(finding.confidence.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("App Forensics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().shadowAndBorder(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cryptographic Signature (SHA-256)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.signatureHash.takeIf { it.isNotEmpty() && it != "Unavailable" } ?: "Signature Extraction Blocked", 
                            style = MaterialTheme.typography.bodySmall, 
                            fontWeight = FontWeight.SemiBold, 
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Usage Telemetry", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        val days = if (app.lastTimeUsed > 0) ((System.currentTimeMillis() - app.lastTimeUsed) / (1000L * 60 * 60 * 24)).toString() + " days ago" else "Unknown / Telemetry Blocked"
                        Text(
                            text = "Last Process Execution: $days", 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                val json = org.json.JSONObject().apply {
                                    put("package_name", app.packageName)
                                    put("app_name", app.appName)
                                    put("risk_score", app.riskResult.totalScore)
                                    put("severity", app.riskResult.severity.name)
                                    
                                    val findingsArr = org.json.JSONArray()
                                    app.findings.forEach { finding ->
                                        findingsArr.put(org.json.JSONObject().apply {
                                            put("title", finding.title)
                                            put("description", finding.description)
                                            put("severity", finding.severity.name)
                                            put("confidence", finding.confidence.name)
                                            put("reason", finding.reason)
                                            put("mitigation", finding.mitigation)
                                        })
                                    }
                                    put("findings", findingsArr)
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, json.toString(4))
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Export JSON Findings"))
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export JSON Audit", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Permissions Flow
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Requested Permissions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                if (app.permissions.isEmpty()) {
                    Text("No permissions requested.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        app.permissions.forEach { perm ->
                            PermissionChip(permission = perm.toReadablePermission())
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = riskColor),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Revoke Permissions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PermissionChip(permission: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = permission,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

fun String.toReadablePermission(): String {
    return this.substringAfterLast(".").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}
