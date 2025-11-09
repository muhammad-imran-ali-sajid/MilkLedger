package com.miassolutions.milkledger.data.remote.model

// Data class used exclusively for interacting with Firestore
data class FirestoreSupplier(
    val supplierId: String = "",
    val supplierName: String = "",
    val supplierRate: Double = 0.0,
    val sortOrder: Int = 0,
    val advanceAmount : Double = 0.0,

    // 💡 IMPORTANT: Changed to String for Firestore compatibility
    val createdAt: String = "",

    val isDefault: Boolean = false,
    val isSynced: Boolean = false,
    val updatedAt: String = "",
    val deletedAt: Long? = null
)