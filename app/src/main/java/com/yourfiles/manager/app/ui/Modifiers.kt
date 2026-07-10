package com.yourfiles.manager.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.uim3.theme.Spacing

object AppModifiers {
    // Screen-level
    @Composable
    fun screenPadding() = Modifier.padding(
        horizontal = Spacing.screenHorizontal,
        vertical = Spacing.screenVertical
    )

    // Card modifiers
    fun cardModifier() = Modifier
        .fillMaxWidth()
        .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)

    fun cardInner() = Modifier.padding(Spacing.cardInner)

    // List item
    fun listItem() = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .padding(horizontal = Spacing.listItemHorizontal, vertical = Spacing.sm)

    // Touch target
    fun touchTarget(minSize: Int = 48) = Modifier.sizeIn(
        minWidth = minSize.dp,
        minHeight = minSize.dp
    )

    // Icon button with proper touch target
    fun iconButton() = Modifier.size(Spacing.iconLarge)
}

fun iconModifier(): Modifier = Modifier
    .size(48.dp)
    .padding(12.dp)