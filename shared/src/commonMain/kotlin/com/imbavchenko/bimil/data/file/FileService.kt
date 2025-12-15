package com.imbavchenko.bimil.data.file

interface FileService {
    suspend fun saveFile(fileName: String, data: ByteArray, mimeType: String): Boolean
    suspend fun readFile(): ByteArray?
}
