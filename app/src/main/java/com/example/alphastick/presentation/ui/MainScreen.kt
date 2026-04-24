package com.example.alphastick.presentation.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphastick.domain.model.AppInfo
import com.example.alphastick.presentation.state.SortOption
import com.example.alphastick.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AlphaStick", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "Sort Options")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Risk Score", fontWeight = FontWeight.Medium) },
                            onClick = { 
                                viewModel.onSortOptionSelected(SortOption.RISK_SCORE)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Install Date", fontWeight = FontWeight.Medium) },
                            onClick = { 
                                viewModel.onSortOptionSelected(SortOption.INSTALL_DATE)
                                showSortMenu = false
                            }
                        )
                    }

                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings", fontWeight = FontWeight.Medium) },
                            onClick = {
                                onSettingsClick()
                                showMoreMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(bgGradient).padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardHeader(apps = state.filteredApps)
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Installed Applications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(items = state.filteredApps, key = { it.packageName }) { app ->
                            AppListItem(app = app, onClick = { onAppClick(app.packageName) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardHeader(apps: List<AppInfo>) {
    val totalApps = apps.size
    val safeApps = apps.count { it.riskResult.severity == com.example.alphastick.domain.model.Severity.INFO || it.riskResult.severity == com.example.alphastick.domain.model.Severity.LOW }
    val riskyApps = totalApps - safeApps
    
    val safetyScore = if (totalApps == 0) 100 else ((safeApps.toFloat() / totalApps) * 100).toInt()
    val riskyScore = if (totalApps == 0) 0 else ((riskyApps.toFloat() / totalApps) * 100).toInt()
    
    val playStoreApps = apps.count { it.isLegitimate }
    val unknownApps = totalApps - playStoreApps
    val playStorePercentage = if (totalApps == 0) 0 else ((playStoreApps.toFloat() / totalApps) * 100).toInt()
    
    val card1Percentage = riskyScore
    
    val card1Status = when {
        card1Percentage <= 40 -> "Protected"
        card1Percentage <= 60 -> "Low Risk"
        else -> "Vulnerable"
    }
    
    val card1Color = when {
        card1Percentage <= 40 -> Color(0xFF4CAF50) // Green
        card1Percentage <= 60 -> Color(0xFFFFAE42) // Yellow Orange
        else -> Color(0xFFFF4B4B) // Red
    }
    
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            if (page == 0) {
                DashboardCard(
                    title = "Device Protection",
                    statusText = card1Status,
                    percentage = card1Percentage,
                    stat1Count = safeApps,
                    stat1Label = "Safe",
                    stat1Color = Color(0xFF81C784),
                    stat2Count = riskyApps,
                    stat2Label = "Risky",
                    stat2Color = Color(0xFFFF4B4B),
                    baseColor = card1Color
                )
            } else {
                DashboardCard(
                    title = "Installation Sources",
                    statusText = "App Origins",
                    percentage = playStorePercentage,
                    stat1Count = playStoreApps,
                    stat1Label = "Play Store",
                    stat1Color = Color(0xFF81C784),
                    stat2Count = unknownApps,
                    stat2Label = "Unknown",
                    stat2Color = Color(0xFFFF4B4B),
                    baseColor = MaterialTheme.colorScheme.primary,
                    isPieChart = true
                )
            }
        }
        
        // Pager Indicators
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    statusText: String,
    percentage: Int,
    stat1Count: Int,
    stat1Label: String,
    stat1Color: Color,
    stat2Count: Int,
    stat2Label: String,
    stat2Color: Color,
    baseColor: Color,
    isPieChart: Boolean = false
) {
    val dashGradient = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.6f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadowAndBorder(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(dashGradient)
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(statusText, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(stat1Color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$stat1Count $stat1Label", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(stat2Color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$stat2Count $stat2Label", color = Color.White, fontSize = 12.sp)
                    }
                }
                
                // Circular Score Indicator or Pie Chart
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    if (isPieChart) {
                        val total = stat1Count + stat2Count
                        if (total > 0) {
                            val perc1 = ((stat1Count.toFloat() / total) * 100).toInt()
                            val perc2 = ((stat2Count.toFloat() / total) * 100).toInt()
                            val sweep1 = (stat1Count.toFloat() / total) * 360f
                            val sweep2 = (stat2Count.toFloat() / total) * 360f
                            
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(70.dp)) {
                                drawArc(
                                    color = stat1Color,
                                    startAngle = -90f,
                                    sweepAngle = sweep1,
                                    useCenter = true
                                )
                                drawArc(
                                    color = stat2Color,
                                    startAngle = -90f + sweep1,
                                    sweepAngle = sweep2,
                                    useCenter = true
                                )
                                
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 34f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                
                                val radius = size.minDimension / 2
                                val centerOffset = center
                                
                                if (perc1 > 0) {
                                    val angle1 = (-90f + sweep1 / 2) * (Math.PI / 180f)
                                    val textRad = radius * 0.55f
                                    val x = centerOffset.x + textRad * kotlin.math.cos(angle1).toFloat()
                                    val y = centerOffset.y + textRad * kotlin.math.sin(angle1).toFloat() + 10f
                                    drawContext.canvas.nativeCanvas.drawText("$perc1%", x, y, paint)
                                }
                                
                                if (perc2 > 0) {
                                    val angle2 = (-90f + sweep1 + sweep2 / 2) * (Math.PI / 180f)
                                    val textRad = radius * 0.55f
                                    val x = centerOffset.x + textRad * kotlin.math.cos(angle2).toFloat()
                                    val y = centerOffset.y + textRad * kotlin.math.sin(angle2).toFloat() + 10f
                                    drawContext.canvas.nativeCanvas.drawText("$perc2%", x, y, paint)
                                }
                            }
                        } else {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(70.dp)) {
                                drawCircle(color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                    } else {
                        CircularProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text("$percentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    val riskColor = when (app.riskResult.severity) {
        com.example.alphastick.domain.model.Severity.CRITICAL -> Color(0xFFFF4B4B)
        com.example.alphastick.domain.model.Severity.HIGH -> Color(0xFFFF4B4B)
        com.example.alphastick.domain.model.Severity.MEDIUM -> Color(0xFFFFAE42)
        com.example.alphastick.domain.model.Severity.LOW -> Color(0xFF81C784)
        com.example.alphastick.domain.model.Severity.INFO -> Color(0xFF4CAF50)
    }
    val riskBg = riskColor.copy(alpha = 0.1f)
    val riskName = app.riskResult.severity.name
    
    val initialLabel = app.appName.take(1).uppercase()
    val initialGradient = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick)
            .shadowAndBorder(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                var appIcon by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                
                LaunchedEffect(app.packageName) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val drawable = context.packageManager.getApplicationIcon(app.packageName)
                            val bitmap = drawable.toBitmap(128, 128)
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
                    Box(modifier = Modifier.fillMaxSize().background(initialGradient, CircleShape), contentAlignment = Alignment.Center) {
                        Text(initialLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill=false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(20.dp)
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
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Score: ${app.riskResult.totalScore} • Target: ${app.packageName.take(25)}...", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
             Box(
                modifier = Modifier
                    .background(riskBg, RoundedCornerShape(12.dp))
                    .border(1.dp, riskColor.copy(alpha=0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = riskName, 
                    color = riskColor, 
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Utility for soft borders and subtle styles
fun Modifier.shadowAndBorder(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp)): Modifier = this.then(
    Modifier.border(0.5.dp, Color.Gray.copy(alpha = 0.15f), shape)
)
