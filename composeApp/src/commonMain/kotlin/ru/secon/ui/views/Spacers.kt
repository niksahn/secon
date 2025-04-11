package com.niksah.gagarin.utils.views

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun RowScope.Spacer(size: Dp) {
	androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(size))
}

@Composable
fun ColumnScope.Spacer(size: Dp) {
	androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(size))
}