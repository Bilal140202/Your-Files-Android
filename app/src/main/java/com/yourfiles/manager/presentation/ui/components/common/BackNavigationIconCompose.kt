package com.yourfiles.manager.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.yourfiles.manager.app.LocalNavController

@Composable
fun BackNavigationIconCompose() {
    val navController = LocalNavController.current
    if (navController.previousBackStackEntry != null) {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
            )
        }
    }
}