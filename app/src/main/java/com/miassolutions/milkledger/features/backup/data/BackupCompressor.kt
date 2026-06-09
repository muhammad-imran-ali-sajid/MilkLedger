package com.miassolutions.milkledger.features.backup.data


import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object BackupCompressor {
    
    fun gzip(input: String): ByteArray {
        val output = ByteArrayOutputStream()
        
        GZIPOutputStream(output).use { gzip ->
            gzip.write(input.toByteArray(Charsets.UTF_8))
        }
        
        return output.toByteArray()
    }
    
    fun ungzip(input: ByteArray): String {
        val output = ByteArrayOutputStream()
        
        GZIPInputStream(ByteArrayInputStream(input)).use { gzip ->
            gzip.copyTo(output)
        }
        
        return output.toString(Charsets.UTF_8.name())
    }
}