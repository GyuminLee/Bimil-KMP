package com.imbavchenko.bimil.presentation.component

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePickerLaunchers(
    onFileSaved: (Boolean) -> Unit,
    onFileSelected: (ByteArray?) -> Unit
): FilePickerLaunchers {
    val context = LocalContext.current
    var pendingData by remember { mutableStateOf<ByteArray?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null && pendingData != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pendingData)
                }
                onFileSaved(true)
            } catch (e: Exception) {
                onFileSaved(false)
            }
        } else {
            onFileSaved(false)
        }
        pendingData = null
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val data = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
                onFileSelected(data)
            } catch (e: Exception) {
                onFileSelected(null)
            }
        } else {
            onFileSelected(null)
        }
    }

    return remember(createDocumentLauncher, openDocumentLauncher) {
        FilePickerLaunchers(
            launchFileSaver = { data, fileName ->
                pendingData = data
                createDocumentLauncher.launch(fileName)
            },
            launchFilePicker = {
                openDocumentLauncher.launch(arrayOf("*/*"))
            }
        )
    }
}
