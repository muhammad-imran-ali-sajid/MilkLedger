package com.miassolutions.milkledger.features.backup.data


import java.security.MessageDigest

object BackupChecksum {
    
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        
        return bytes.joinToString("") { "%02x".format(it) }
    }
}