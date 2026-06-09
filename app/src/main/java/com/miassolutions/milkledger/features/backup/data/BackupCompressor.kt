package com.miassolutions.milkledger.features.backup.data


import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object BackupCompressor {
    
    fun gzip(input: String): ByteArray {
        val output = ByteArrayOutputStream()
        
        GZIPOutputStream(output).use { gzip ->
            gzip.write(input.toByteArray(Charsets.UTF_8))
        }
        
        return output.toByteArray()
    }
}