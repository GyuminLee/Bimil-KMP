package com.imbavchenko.bimil.presentation.component

import androidx.compose.runtime.Composable

data class FilePickerLaunchers(
    val launchFileSaver: (ByteArray, String) -> Unit,
    val launchFilePicker: () -> Unit
)

@Composable
expect fun rememberFilePickerLaunchers(
    onFileSaved: (Boolean) -> Unit,
    onFileSelected: (ByteArray?) -> Unit
): FilePickerLaunchers
