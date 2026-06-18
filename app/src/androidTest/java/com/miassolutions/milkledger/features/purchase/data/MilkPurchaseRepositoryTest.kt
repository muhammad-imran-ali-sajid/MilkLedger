package com.miassolutions.milkledger.features.purchase.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.core.localdb.milk.TransactionType
import com.miassolutions.milkledger.features.backup.data.BackupFileManager
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.data.BackupValidator
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveBackupDataSource
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveServiceFactory
import com.miassolutions.milkledger.features.purchase.model.UpdatePurchaseRequest
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MilkPurchaseRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupPrefs: BackupPrefs
    private lateinit var repository: MilkPurchaseRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        backupPrefs = BackupPrefs(context)

        val backupRepository = BackupRepository(
            database = database,
            backupFileManager = BackupFileManager(context),
            backupValidator = BackupValidator(),
            googleDriveBackupDataSource = GoogleDriveBackupDataSource(
                GoogleDriveServiceFactory(context)
            ),
            backupPrefs = backupPrefs
        )

        repository = MilkPurchaseRepository(
            accountDao = database.accountDao(),
            milkDao = database.milkDao(),
            ledgerDao = database.ledgerDao(),
            backupRepository = backupRepository,
            db = database
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveMilkPurchase_createsMilkTransactionPurchaseLedgerAndCashPaidLedger() = runBlocking {
        seedSupplier()

        val purchaseDate = LocalDate.of(2026, 1, 15)
        repository.saveMilkPurchase(
            supplierId = SUPPLIER_ID,
            date = purchaseDate,
            paymentDate = purchaseDate,
            volume = 13.0,
            fat = 4.0,
            lr = 28.0,
            rate = 100.0,
            amountPaid = 50_000L,
            note = "Morning purchase"
        )

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertEquals(SUPPLIER_ID, milk.accountId)
        assertEquals(purchaseDate.toMillis(), milk.dateMillis)
        assertEquals(purchaseDate.toMillis(), milk.paymentDateMillis)
        assertEquals(TransactionType.PURCHASE, milk.type)
        assertEquals(13.0, milk.volume, 0.0)
        assertEquals(0.0, milk.deduction, 0.0)
        assertEquals(13.0, milk.quantity, 0.0)
        assertEquals(4.0, milk.fat ?: 0.0, 0.0)
        assertEquals(28.0, milk.lr ?: 0.0, 0.0)
        assertEquals(12.6, milk.ts ?: 0.0, 0.000001)
        assertEquals(100.0, milk.rateUsed, 0.0)
        assertEquals(125_999L, milk.totalAmount)
        assertEquals("Morning purchase", milk.notes)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)

        val purchaseLedger = ledgers.single { it.type == LedgerEntryType.MILK_PURCHASE }
        assertEquals(milk.milkTransId, purchaseLedger.referenceId)
        assertEquals(SUPPLIER_ID, purchaseLedger.accountId)
        assertEquals(purchaseDate.toMillis(), purchaseLedger.dateMillis)
        assertEquals(0L, purchaseLedger.debit)
        assertEquals(125_999L, purchaseLedger.credit)
        assertEquals(-125_999L, purchaseLedger.profitImpact)
        assertEquals("Purchase: 13.0 Ltr (F:4.0, L:28.0)", purchaseLedger.note)

        val paymentLedger = ledgers.single { it.type == LedgerEntryType.CASH_PAID }
        assertEquals(milk.milkTransId, paymentLedger.referenceId)
        assertEquals(SUPPLIER_ID, paymentLedger.accountId)
        assertEquals(purchaseDate.toMillis(), paymentLedger.dateMillis)
        assertEquals(50_000L, paymentLedger.debit)
        assertEquals(0L, paymentLedger.credit)
        assertEquals(0L, paymentLedger.profitImpact)
        assertEquals(SUPPLIER_NAME, paymentLedger.note)

        assertTrue(backupPrefs.getLastDataChangedAt() > 0L)
    }

    @Test
    fun updateMilkPurchase_updatesMilkTransactionAndRelatedLedgers() = runBlocking {
        seedSupplier()

        val originalDate = LocalDate.of(2026, 1, 15)
        repository.saveMilkPurchase(
            supplierId = SUPPLIER_ID,
            date = originalDate,
            paymentDate = originalDate,
            volume = 13.0,
            fat = 4.0,
            lr = 28.0,
            rate = 100.0,
            amountPaid = 10_000L,
            note = "Original purchase"
        )

        val purchaseId = database.milkDao().getAllMilkTransactionsForBackup().single().milkTransId
        val updatedDate = LocalDate.of(2026, 1, 16)
        val paymentDate = LocalDate.of(2026, 1, 17)

        repository.updateMilkPurchase(
            UpdatePurchaseRequest(
                purchaseId = purchaseId,
                supplierId = SUPPLIER_ID,
                date = updatedDate,
                paymentDate = paymentDate,
                volume = 13.0,
                fat = 5.0,
                lr = 30.0,
                rate = 100.0,
                amountPaid = 20_000L,
                note = "Updated purchase"
            )
        )

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertEquals(purchaseId, milk.milkTransId)
        assertEquals(updatedDate.toMillis(), milk.dateMillis)
        assertEquals(paymentDate.toMillis(), milk.paymentDateMillis)
        assertEquals(13.0, milk.volume, 0.0)
        assertEquals(5.0, milk.fat ?: 0.0, 0.0)
        assertEquals(30.0, milk.lr ?: 0.0, 0.0)
        assertEquals(14.32, milk.ts ?: 0.0, 0.000001)
        assertEquals(13.0, milk.quantity, 0.0)
        assertEquals(100.0, milk.rateUsed, 0.0)
        assertEquals(143_200L, milk.totalAmount)
        assertEquals("Updated purchase", milk.notes)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)

        val purchaseLedger = ledgers.single { it.type == LedgerEntryType.MILK_PURCHASE }
        assertEquals(updatedDate.toMillis(), purchaseLedger.dateMillis)
        assertEquals(0L, purchaseLedger.debit)
        assertEquals(143_200L, purchaseLedger.credit)
        assertEquals(-143_200L, purchaseLedger.profitImpact)
        assertEquals("Purchase: 13.0 Ltr (F:5.0, L:30.0)", purchaseLedger.note)

        val paymentLedger = ledgers.single { it.type == LedgerEntryType.CASH_PAID }
        assertEquals(updatedDate.toMillis(), paymentLedger.dateMillis)
        assertEquals(20_000L, paymentLedger.debit)
        assertEquals(0L, paymentLedger.credit)
        assertEquals(0L, paymentLedger.profitImpact)
        assertEquals("$SUPPLIER_NAME\n(Dated: 17/01)", paymentLedger.note)
    }

    @Test
    fun deletePurchase_softDeletesMilkTransactionAndRelatedLedgers() = runBlocking {
        seedSupplier()

        repository.saveMilkPurchase(
            supplierId = SUPPLIER_ID,
            date = LocalDate.of(2026, 1, 15),
            paymentDate = LocalDate.of(2026, 1, 15),
            volume = 13.0,
            fat = 4.0,
            lr = 28.0,
            rate = 100.0,
            amountPaid = 10_000L,
            note = "Purchase to delete"
        )

        val purchaseId = database.milkDao().getAllMilkTransactionsForBackup().single().milkTransId
        repository.deletePurchase(purchaseId)

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertNotNull(milk.deletedAtMillis)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)
        ledgers.forEach { ledger ->
            assertEquals(purchaseId, ledger.referenceId)
            assertNotNull(ledger.deletedAtMillis)
        }
    }

    private suspend fun seedSupplier() {
        database.accountDao().insert(
            AccountEntity(
                accountId = SUPPLIER_ID,
                name = SUPPLIER_NAME,
                phone = null,
                accountType = AccountType.SUPPLIER,
                sortOrder = 1,
                advanceAmount = 0L,
                defaultRate = 100.0,
                initialBalance = 0L,
                createdAtMillis = LocalDate.of(2026, 1, 1).toMillis()
            )
        )
    }

    private companion object {
        const val SUPPLIER_ID = "supplier-1"
        const val SUPPLIER_NAME = "Bashir Dairy Farm"
    }
}
