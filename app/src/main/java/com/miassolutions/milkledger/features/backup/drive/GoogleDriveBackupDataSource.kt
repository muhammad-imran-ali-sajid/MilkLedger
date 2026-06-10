package com.miassolutions.milkledger.features.backup.drive

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.File as JavaFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupDataSource @Inject constructor(
    private val driveServiceFactory: GoogleDriveServiceFactory
) {
    
    fun uploadBackupFile(localFile: JavaFile): DriveBackupResult {
        val drive = driveServiceFactory.createDriveService()
        
        val folderId = findOrCreateBackupFolder(drive)
        
        val metadata = File().apply {
            name = localFile.name
            parents = listOf(folderId)
            mimeType = "application/octet-stream"
        }
        
        val mediaContent = FileContent(
            "application/octet-stream",
            localFile
        )
        
        val uploadedFile = drive.files()
            .create(metadata, mediaContent)
            .setFields("id, name, webViewLink")
            .execute()
        
        return DriveBackupResult(
            fileId = uploadedFile.id,
            fileName = uploadedFile.name,
            webViewLink = uploadedFile.webViewLink
        )
    }
    
    private fun findOrCreateBackupFolder(drive: Drive): String {
        val folderName = "Milk Ledger Backups"
        
        val query = """
            mimeType = 'application/vnd.google-apps.folder'
            and name = '$folderName'
            and trashed = false
        """.trimIndent()
        
        val result = drive.files()
            .list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
        
        val existingFolder = result.files.firstOrNull()
        if (existingFolder != null) {
            return existingFolder.id
        }
        
        val folderMetadata = File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
        }
        
        val folder = drive.files()
            .create(folderMetadata)
            .setFields("id")
            .execute()
        
        return folder.id
    }
    
    fun listBackupFiles(): List<DriveBackupFile> {
        val drive = driveServiceFactory.createDriveService()
        
        val folderId = findOrCreateBackupFolder(drive)
        
        val query = """
        '$folderId' in parents
        and trashed = false
        and name contains '.mlbackup'
    """.trimIndent()
        
        val result = drive.files()
            .list()
            .setQ(query)
            .setSpaces("drive")
            .setOrderBy("modifiedTime desc")
            .setFields("files(id, name, size, createdTime, modifiedTime, webViewLink)")
            .execute()
        
        return result.files.map { file ->
            DriveBackupFile(
                fileId = file.id,
                name = file.name,
                sizeBytes = file.size.toLong(),
                createdTimeMillis = file.createdTime?.value,
                modifiedTimeMillis = file.modifiedTime?.value,
                webViewLink = file.webViewLink
            )
        }
    }
    
    fun downloadBackupFile(
        fileId: String,
        destinationFile: java.io.File
    ): java.io.File {
        val drive = driveServiceFactory.createDriveService()
        
        destinationFile.outputStream().use { outputStream ->
            drive.files()
                .get(fileId)
                .executeMediaAndDownloadTo(outputStream)
        }
        
        return destinationFile
    }
    
    
    
    
}