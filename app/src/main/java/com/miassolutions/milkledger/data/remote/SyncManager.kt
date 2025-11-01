package com.miassolutions.milkledger.data.remote

import com.miassolutions.milkledger.data.local.daos.CustomerDao
import jakarta.inject.Inject

class SyncManager @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestoreService: FirestoreService
) {
    suspend fun syncCustomers() {
//        // 1. Upload unsynced
//        val localPending = customerDao.getPendingSync()
//        for (c in localPending) firestoreService.uploadCustomer(c)
//
//        // 2. Download newer
//        val remote = firestoreService.downloadCustomers()
//        remote.forEach { remoteCustomer ->
//            val local = customerDao.getCustomerByIdOnce(remoteCustomer.customerId)
//            if (local == null || remoteCustomer.updatedAt > local.updatedAt) {
//                customerDao.insertCustomer(remoteCustomer.copy(isSynced = true))
//            }
//        }
    }
}
