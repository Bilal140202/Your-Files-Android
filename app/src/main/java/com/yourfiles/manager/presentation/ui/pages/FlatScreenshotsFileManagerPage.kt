package com.yourfiles.manager.presentation.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.R
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import com.yourfiles.manager.presentation.ui.components.CATEGORY_SCREENSHOTS
import com.yourfiles.manager.presentation.ui.components.common.flatFileManager.FlatFileManagerContent
import com.yourfiles.manager.presentation.ui.components.common.flatFileManager.FlatFileManagerDeleteComposable
import com.yourfiles.manager.presentation.vm.FlatScreenshotsFileManagerVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatScreenshotsFileManager(vm: FlatScreenshotsFileManagerVM = viewModel()) {
    val selectedModeOn = remember { vm.selectedModeOn }
    val files by remember { vm.getScreenshotFiles() }.collectAsState(initial = null)
    val configuration = LocalConfiguration.current
    val columns = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
    val thumbnailSize = configuration.screenWidthDp.dp / columns

    // Sort chips state
    var sortBy by remember { mutableStateOf("date") }
    val sortOptions = listOf("date", "size", "name")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_screenshots)) },
                navigationIcon = { BackNavigationIconCompose() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        floatingActionButton = {
            if (selectedModeOn.value) {
                val selectedSize = (files?.filter { it.id in vm.selectedFiles.value }?.sumOf { it.size } ?: 0L) * 1024
                FlatFileManagerDeleteComposable(vm, selectedSize)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            FlatFileManagerContent(files, columns, thumbnailSize, vm, category = CATEGORY_SCREENSHOTS)
        }
    }
}
