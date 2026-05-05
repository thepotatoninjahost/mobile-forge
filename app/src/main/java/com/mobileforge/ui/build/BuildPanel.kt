package com.mobileforge.ui.build

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileforge.ui.IDEViewModel
import com.mobileforge.ui.theme.*

@Composable
fun BuildPanel(viewModel: IDEViewModel) {
    var gradleTask by remember { mutableStateOf("assembleDebug") }
    val result = viewModel.buildResult

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Build, "Build", 24.dp, tint = AccentBlue)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Build & Compile", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Compile your Android project to APK", fontSize = 12.sp, color = Color(0xFF888888))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Build targets
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Build Targets", fontSize = 14.sp, color = Color(0xFF888888))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gradleTask == "assembleDebug",
                        onClick = { gradleTask = "assembleDebug" }
                    )
                    Text("Debug APK", fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = gradleTask == "assembleRelease",
                        onClick = { gradleTask = "assembleRelease" }
                    )
                    Text("Release APK", fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Build button
        Button(
            onClick = { viewModel.buildProject() },
            enabled = !viewModel.buildInProgress,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            if (viewModel.buildInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text("Building...", fontSize = 16.sp)
            } else {
                Icon(Icons.Default.PlayArrow, "Build", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("BUILD APK", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Build output
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Output, "Output", 18.dp, tint = Color(0xFF888888))
                    Spacer(Modifier.width(8.dp))
                    Text("Build Output", fontSize = 14.sp, color = Color(0xFF888888))
                }
                Spacer(Modifier.height(8.dp))
                if (result != null) {
                    if (result.success) {
                        Row { Icon(Icons.Default.CheckCircle, "OK", 20.dp, tint = AccentGreen); Spacer(Modifier.width(8.dp)); Text("Build Successful!", color = AccentGreen, fontSize = 14.sp) }
                        Spacer(Modifier.height(8.dp))
                        result.outputPath?.let { Text("APK: $it", color = Color(0xFFD4D4D4), fontSize = 12.sp) }
                    } else {
                        Row { Icon(Icons.Default.Error, "FAIL", 20.dp, tint = AccentRed); Spacer(Modifier.width(8.dp)); Text("Build Failed", color = AccentRed, fontSize = 14.sp) }
                        result.errors.forEach { Text(it, color = AccentRed, fontSize = 12.sp) }
                    }
                } else {
                    Text("No build output yet. Click BUILD APK to compile.", color = Color(0xFF555555), fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { /* clean */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Cleaning, "Clean", 16.dp); Spacer(Modifier.width(4.dp)); Text("Clean")
            }
            OutlinedButton(onClick = { /* rebuild */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, "Rebuild", 16.dp); Spacer(Modifier.width(4.dp)); Text("Rebuild")
            }
            OutlinedButton(onClick = { /* install */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Install, "Install", 16.dp); Spacer(Modifier.width(4.dp)); Text("Install")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}