package com.mobileforge.ui.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileforge.ui.IDEViewModel
import com.mobileforge.ui.theme.*

@Composable
fun EditorPanel(viewModel: IDEViewModel) {
    val tabs = viewModel.editorTabs
    val activeIndex = viewModel.activeTabIndex

    Column(modifier = Modifier.fillMaxSize().background(DarkEditor)) {
        // Tab bar
        if (tabs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(DarkSurface)
                    .horizontalScroll(rememberScrollState())
            ) {
                tabs.forEachIndexed { index, tab ->
                    TabItem(
                        name = tab.fileName,
                        isActive = index == activeIndex,
                        isModified = tab.isModified,
                        onSelect = { viewModel.activeTabIndex = index },
                        onClose = { viewModel.closeTab(index) }
                    )
                }
            }
        }

        // Toolbar
        EditorToolbar(
            canSave = tabs.isNotEmpty() && activeIndex >= 0 && tabs.getOrNull(activeIndex)?.isModified == true,
            language = viewModel.currentLanguage,
            onSave = { viewModel.saveCurrentTab() },
            onUndo = { /* handled in editor */ },
            onRedo = { /* handled in editor */ },
            onFind = { /* handled in editor */ }
        )

        // Editor area
        if (tabs.isNotEmpty() && activeIndex >= 0) {
            val activeTab = tabs[activeIndex]
            CodeEditor(
                content = activeTab.content,
                language = activeTab.language ?: viewModel.currentLanguage,
                onContentChange = { viewModel.updateTabContent(it) },
                modifier = Modifier.weight(1f)
            )
        } else {
            EmptyEditorState()
        }
    }
}

@Composable
fun TabItem(name: String, isActive: Boolean, isModified: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    Surface(
        onClick = onSelect,
        color = if (isActive) DarkEditor else DarkSurface,
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                color = if (isActive) Color.White else Color(0xFF888888)
            )
            if (isModified) {
                Text(" ●", color = AccentOrange, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close, "Close",
                14.dp, tint = Color(0xFF888888),
                modifier = Modifier.clickable { onClose() }
            )
        }
    }
}

@Composable
fun EditorToolbar(
    canSave: Boolean,
    language: String,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFind: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSave, enabled = canSave) {
            Icon(Icons.Default.Save, "Save", tint = if (canSave) AccentBlue else Color(0xFF555555))
        }
        IconButton(onClick = onUndo) {
            Icon(Icons.Default.Undo, "Undo", tint = Color(0xFF888888))
        }
        IconButton(onClick = onRedo) {
            Icon(Icons.Default.Redo, "Redo", tint = Color(0xFF888888))
        }
        IconButton(onClick = onFind) {
            Icon(Icons.Default.Search, "Find", tint = Color(0xFF888888))
        }
        Spacer(Modifier.weight(1f))
        Text(language.uppercase(), color = AccentBlue, fontSize = 11.sp)
    }
}

@Composable
fun CodeEditor(
    content: String,
    language: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = content.split('\n')
    val lineCount = lines.size
    val scrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()

    var textStyle by remember {
        mutableStateOf(
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color(0xFFD4D4D4),
                lineHeight = 20.sp
            )
        )
    }

    Row(modifier = modifier.background(DarkEditor).fillMaxSize()) {
        // Line numbers
        Column(
            modifier = Modifier
                .width(48.dp)
                .background(DarkSurface)
                .padding(top = 8.dp)
                .verticalScroll(scrollState)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.End
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString(),
                    style = textStyle.copy(color = LineNumbers),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )
            }
        }

        VerticalDivider(color = Color(0xFF333333), thickness = 1.dp)

        // Code area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(AccentBlue),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmptyEditorState() {
    Box(
        modifier = Modifier.fillMaxSize().background(DarkEditor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Code, "Empty",
                64.dp, Color(0xFF333333)
            )
            Spacer(Modifier.height(16.dp))
            Text("No file open", color = Color(0xFF555555), fontSize = 16.sp)
            Text(
                "Select a file from the Files panel or create a new one",
                color = Color(0xFF444444),
                fontSize = 13.sp
            )
        }
    }
}
