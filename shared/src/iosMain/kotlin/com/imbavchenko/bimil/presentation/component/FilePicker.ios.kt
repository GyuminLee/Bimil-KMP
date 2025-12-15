package com.imbavchenko.bimil.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberFilePickerLaunchers(
    onFileSaved: (Boolean) -> Unit,
    onFileSelected: (ByteArray?) -> Unit
): FilePickerLaunchers {
    return remember {
        FilePickerLaunchers(
            launchFileSaver = { data, fileName ->
                try {
                    val fileManager = NSFileManager.defaultManager
                    val documentsPath = fileManager.URLsForDirectory(
                        NSDocumentDirectory,
                        NSUserDomainMask
                    ).firstOrNull() as? NSURL

                    if (documentsPath != null) {
                        val fileUrl = documentsPath.URLByAppendingPathComponent(fileName)

                        val nsData = data.usePinned { pinned ->
                            NSData.dataWithBytes(pinned.addressOf(0), data.size.toULong())
                        }

                        if (fileUrl != null && nsData != null) {
                            val success = nsData.writeToURL(fileUrl, atomically = true)

                            if (success) {
                                // Share the file using UIActivityViewController
                                val rootViewController = UIApplication.sharedApplication
                                    .keyWindow?.rootViewController

                                if (rootViewController != null) {
                                    val activityVC = UIActivityViewController(
                                        activityItems = listOf(fileUrl),
                                        applicationActivities = null
                                    )
                                    rootViewController.presentViewController(
                                        activityVC,
                                        animated = true,
                                        completion = null
                                    )
                                }
                            }
                            onFileSaved(success)
                        } else {
                            onFileSaved(false)
                        }
                    } else {
                        onFileSaved(false)
                    }
                } catch (e: Exception) {
                    onFileSaved(false)
                }
            },
            launchFilePicker = {
                try {
                    val rootViewController = UIApplication.sharedApplication
                        .keyWindow?.rootViewController

                    if (rootViewController != null) {
                        val documentPicker = UIDocumentPickerViewController(
                            documentTypes = listOf("public.data"),
                            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
                        )

                        val delegate = FilePickerDelegate(onFileSelected)
                        documentPicker.delegate = delegate

                        rootViewController.presentViewController(
                            documentPicker,
                            animated = true,
                            completion = null
                        )
                    } else {
                        onFileSelected(null)
                    }
                } catch (e: Exception) {
                    onFileSelected(null)
                }
            }
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class FilePickerDelegate(
    private val onFileSelected: (ByteArray?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url != null) {
            try {
                val data = NSData.create(contentsOfURL = url)
                if (data != null) {
                    val bytes = ByteArray(data.length.toInt())
                    bytes.usePinned { pinned ->
                        platform.posix.memcpy(
                            pinned.addressOf(0),
                            data.bytes,
                            data.length
                        )
                    }
                    onFileSelected(bytes)
                } else {
                    onFileSelected(null)
                }
            } catch (e: Exception) {
                onFileSelected(null)
            }
        } else {
            onFileSelected(null)
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onFileSelected(null)
    }
}
