package com.imbavchenko.bimil.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePickerLaunchers(
    onFileSaved: (Boolean) -> Unit,
    onFileSelected: (ByteArray?) -> Unit
): FilePickerLaunchers {
    return remember {
        FilePickerLaunchers(
            launchFileSaver = { data, fileName ->
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Save Backup"
                    selectedFile = File(fileName)
                    fileFilter = FileNameExtensionFilter("Backup files", "bak")
                }

                val result = fileChooser.showSaveDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        var file = fileChooser.selectedFile
                        if (!file.name.endsWith(".bak")) {
                            file = File(file.absolutePath + ".bak")
                        }
                        file.writeBytes(data)
                        onFileSaved(true)
                    } catch (e: Exception) {
                        onFileSaved(false)
                    }
                } else {
                    onFileSaved(false)
                }
            },
            launchFilePicker = {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Select Backup File"
                    fileFilter = FileNameExtensionFilter("Backup files", "bak")
                }

                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        val data = fileChooser.selectedFile.readBytes()
                        onFileSelected(data)
                    } catch (e: Exception) {
                        onFileSelected(null)
                    }
                } else {
                    onFileSelected(null)
                }
            }
        )
    }
}
