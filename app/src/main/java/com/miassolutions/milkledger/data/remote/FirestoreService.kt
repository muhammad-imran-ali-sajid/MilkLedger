package com.miassolutions.milkledger.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun uploadCustomer(customer: CustomerEntity) {
        firestore.collection("customers")
            .document(customer.customerId)
            .set(customer.toFirestoreMap()).await()
    }

    suspend fun downloadCustomers(): List<CustomerEntity> {
        val snapshot = firestore.collection("customers").get().await()
        return snapshot.documents.map { it.toCustomerEntity() }
    }
}