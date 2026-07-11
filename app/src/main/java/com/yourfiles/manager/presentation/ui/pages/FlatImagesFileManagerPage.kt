package com.yourfiles.manager.presentation.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.R
import com.yourfiles.manager.app.LocalNavController
import com.yourfiles.manager.presentation.ui.components.CATEGORY_IMAGES
import com.yourfiles.manager.presentation.ui.components.common.EmptyStateView
import com.yourfiles.manager.presentation.ui.components.common.LoadingStateView
import com.yourfiles.manager.presentation.ui.components.common.ScreenScaffold
import com.yourfiles.manager.presentation.ui.components.common.flatFileManager.FlatFileManagerContent
import com.yourfiles.manager.presentation.ui.components.common.flatFileManager.FlatFileManagerDeleteComposable
import com.yourfiles.manager.presentation.vm.FlatImagesFileManagerVM

@Composable
fun FlatImagesFileManager(
    onOpenDrawer: () -> Unit = {},
    vm: FlatImagesFileManagerVM = viewModel(),
) {
    val navController = LocalNavController.current
    val selectedModeOn = remember { vm.selectedModeOn }
    val filesState by remember { vm.getImageFiles() }.collectAsState(initial = null)
    val files = filesState
    val configuration = LocalConfiguration.current
    val columns = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
    val thumbnailSize = configuration.screenWidthDp.dp / columns

    ScreenScaffold(
        title = stringResource(R.string.category_large_images),
        onOpenDrawer = onOpenDrawer,
        actions = {
            if (!selectedModeOn.value) {
                TextButton(onClick = { selectedModeOn.value = true }) {
                    Text(stringResource(R.string.action_select))
                }
            } else {
                TextButton(onClick = {
                    val allIds = files?.map { it.id }?.toSet() ?: emptySet()
                    if (vm.selectedFiles.value.size == allIds.size) vm.selectedFiles.value = emptySet()
                    else vm.selectedFiles.value = allIds
                }) {
                    Text(
                        if ((files?.map { it.id }?.toSet() ?: emptySet()).size == vm.selectedFiles.value.size)
                            stringResource(R.string.action_deselect_all) else stringResource(R.string.action_select_all)
                    )
                }
                TextButton(onClick = { selectedModeOn.value = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        },
        floatingActionButton = {
            if (selectedModeOn.value) {
                val selectedSize = (files?.filter { it.id in vm.selectedFiles.value }?.sumOf { it.size } ?: 0L) * 1024
                FlatFileManagerDeleteComposable(vm, selectedSize)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                files == null -> {
                    LoadingStateView()
                }
                files.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Image,
                        title = "No large images found",
                        subtitle = "All your images are already optimized",
                    )
                }
                else -> {
                    FlatFileManagerContent(files, columns, thumbnailSize, vm, category = CATEGORY_IMAGES)
                }
            }
        }
    }
}