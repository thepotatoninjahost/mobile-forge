package com.mobileforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileforge.ui.editor.EditorPanel
import com.mobileforge.ui.files.FilePanel
import com.mobileforge.ui.build.BuildPanel
import com.mobileforge.ui.terminal.TerminalPanel
import com.mobileforge.ui.git.GitPanel

enum class Panel { FILES, EDITOR, TERMINAL, BUILD, GIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: IDEViewModel = viewModel()) {
    var activePanel by remember { mutableStateOf(Panel.EDITOR) }
    var showNav by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Mobile Forge", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { showNav = !showNav }) {
                        Icon(Icons.Default.Menu, "Navigation")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(Icons.Default.DarkMode, "Theme")
                    }
                    IconButton(onClick = { activePanel = Panel.BUILD }) {
                        Icon(Icons.Default.Build, "Build")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Folder, "Files") },
                    label = { Text("Files") },
                    selected = activePanel == Panel.FILES,
                    onClick = { activePanel = Panel.FILES }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Code, "Editor") },
                    label = { Text("Editor") },
                    selected = activePanel == Panel.EDITOR,
                    onClick = { activePanel = Panel.EDITOR }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Terminal, "Terminal") },
                    label = { Text("Terminal") },
                    selected = activePanel == Panel.TERMINAL,
                    onClick = { activePanel = Panel.TERMINAL }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Cloud, "Git") },
                    label = { Text("Git") },
                    selected = activePanel == Panel.GIT,
                    onClick = { activePanel = Panel.GIT }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, "Build") },
                    label = { Text("Build") },
                    selected = activePanel == Panel.BUILD,
                    onClick = { activePanel = Panel.BUILD }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activePanel) {
                Panel.FILES -> FilePanel(viewModel)
                Panel.EDITOR -> EditorPanel(viewModel)
                Panel.TERMINAL -> TerminalPanel(viewModel)
                Panel.BUILD -> BuildPanel(viewModel)
                Panel.GIT -> GitPanel(viewModel)
            }
        }
    }
}
