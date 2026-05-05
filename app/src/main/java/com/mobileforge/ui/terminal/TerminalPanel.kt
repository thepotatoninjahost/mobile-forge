package com.mobileforge.ui.terminal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileforge.ui.IDEViewModel
import com.mobileforge.ui.theme.*

@Composable
fun TerminalPanel(viewModel: IDEViewModel) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.terminalOutput) {
        if (viewModel.terminalOutput.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.terminalOutput.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        // Terminal header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Terminal, "Terminal", 18.dp, tint = AccentGreen)
            Spacer(Modifier.width(8.dp))
            Text("Terminal", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.terminalOutput = emptyList() }) {
                Icon(Icons.Default.Clear, "Clear", 18.dp, tint = Color(0xFF888888))
            }
        }

        // Output area
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(viewModel.terminalOutput) { line ->
                TerminalLine(line)
            }
        }

        // Input area
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$", color = AccentGreen, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                singleLine = true,
                placeholder = { Text("Enter command...", color = Color(0xFF555555)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentGreen
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.runCommand(commandInput)
                        commandInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, "Run", tint = AccentBlue)
            }
        }
    }
}

@Composable
fun TerminalLine(line: String) {
    val color = when {
        line.startsWith("❌") -> AccentRed
        line.startsWith("✅") -> AccentGreen
        line.startsWith("📁") || line.startsWith("📂") -> AccentYellow
        line.startsWith("🔨") || line.startsWith("📦") -> AccentBlue
        line.startsWith("📤") || line.startsWith("📥") -> AccentPurple
        line.startsWith("💾") || line.startsWith("✨") -> AccentGreen
        line.startsWith("$") -> Color(0xFF888888)
        line.startsWith("  ERROR") -> AccentRed
        else -> Color(0xFFD4D4D4)
    }
    Text(
        line,
        color = color,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}