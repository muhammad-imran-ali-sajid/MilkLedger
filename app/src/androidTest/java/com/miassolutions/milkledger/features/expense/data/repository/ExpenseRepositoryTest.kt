package com.miassolutions.milkledger.features.expense.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.contstants.Constants.PREFIX_EXPENSE
import com.miassolutions.milkledger.core.contstants.Constants.SHOP_EXPENSE
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.backup.data.BackupFileManager
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.data.BackupValidator
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveBackupDataSource
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveServiceFactory
import com.miassolutions.milkledger.features.expense.domain.Expense
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
class ExpenseRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupPrefs: BackupPrefs
    private lateinit var repository: ExpenseRepository

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

        repository = ExpenseRepository(
            expenseDao = database.expenseDao(),
            ledgerDao = database.ledgerDao(),
            accountDao = database.accountDao(),
            backupRepository = backupRepository,
            db = database
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveBusinessExpense_createsBusinessExpenseLedgerAndSystemAccounts() = runBlocking {
        val date = LocalDate.of(2026, 1, 15)

        repository.saveExpense(
            Expense(
                expenseId = "",
                date = date,
                title = "Fuel",
                amount = 12_500L,
                category = "Transport",
                isPersonal = false,
                note = "Morning delivery"
            )
        )

        val expense = database.expenseDao().getAllExpensesForBackup().single()
        assertTrue(expense.expenseId.startsWith(PREFIX_EXPENSE))
        assertEquals(date.toMillis(), expense.dateMillis)
        assertEquals("Fuel", expense.title)
        assertEquals(12_500L, expense.amount)
        assertEquals("Transport", expense.category)
        assertEquals(false, expense.isPersonal)
        assertEquals("Morning delivery", expense.note)

        val shopAccount = database.accountDao().getAccountById(SHOP_EXPENSE)
        assertNotNull(shopAccount)
        assertEquals("Shop Expense", shopAccount?.name)
        assertEquals(AccountType.OWNER, shopAccount?.accountType)

        val ownerAccount = database.accountDao().getAccountById(OWNER_ACCOUNT_ID)
        assertNotNull(ownerAccount)
        assertEquals("Owner", ownerAccount?.name)

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(expense.expenseId, ledger.referenceId)
        assertEquals(SHOP_EXPENSE, ledger.accountId)
        assertEquals(date.toMillis(), ledger.dateMillis)
        assertEquals(LedgerEntryType.BUSINESS_EXPENSE, ledger.type)
        assertEquals(12_500L, ledger.debit)
        assertEquals(0L, ledger.credit)
        assertEquals(-12_500L, ledger.profitImpact)
        assertEquals("Fuel - Morning delivery", ledger.note)

        assertTrue(backupPrefs.getLastDataChangedAt() > 0L)
    }

    @Test
    fun savePersonalExpense_createsOwnerDrawingLedgerWithoutProfitImpact() = runBlocking {
        val date = LocalDate.of(2026, 1, 15)

        repository.saveExpense(
            Expense(
                expenseId = "personal-1",
                date = date,
                title = "Home Groceries",
                amount = 8_000L,
                category = "Home",
                isPersonal = true,
                note = null
            )
        )

        val expense = database.expenseDao().getAllExpensesForBackup().single()
        assertEquals("${PREFIX_EXPENSE}personal-1", expense.expenseId)

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(expense.expenseId, ledger.referenceId)
        assertEquals(OWNER_ACCOUNT_ID, ledger.accountId)
        assertEquals(LedgerEntryType.OWNER_DRAWING, ledger.type)
        assertEquals(8_000L, ledger.debit)
        assertEquals(0L, ledger.credit)
        assertEquals(0L, ledger.profitImpact)
        assertEquals("Home Groceries", ledger.note)
    }

    @Test
    fun saveAllExpenses_createsLedgerRowsForBusinessAndPersonalExpenses() = runBlocking {
        repository.saveAllExpenses(
            listOf(
                Expense(
                    expenseId = "batch-business",
                    date = LocalDate.of(2026, 1, 15),
                    title = "Rent",
                    amount = 40_000L,
                    category = "Shop",
                    isPersonal = false,
                    note = null
                ),
                Expense(
                    expenseId = "batch-personal",
                    date = LocalDate.of(2026, 1, 16),
                    title = "Owner Cash",
                    amount = 15_000L,
                    category = "Home",
                    isPersonal = true,
                    note = "Monthly draw"
                )
            )
        )

        val expenses = database.expenseDao().getAllExpensesForBackup()
        assertEquals(2, expenses.size)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)

        val businessLedger = ledgers.single { it.type == LedgerEntryType.BUSINESS_EXPENSE }
        assertEquals(SHOP_EXPENSE, businessLedger.accountId)
        assertEquals(40_000L, businessLedger.debit)
        assertEquals(-40_000L, businessLedger.profitImpact)
        assertEquals("Rent", businessLedger.note)

        val personalLedger = ledgers.single { it.type == LedgerEntryType.OWNER_DRAWING }
        assertEquals(OWNER_ACCOUNT_ID, personalLedger.accountId)
        assertEquals(15_000L, personalLedger.debit)
        assertEquals(0L, personalLedger.profitImpact)
        assertEquals("Owner Cash - Monthly draw", personalLedger.note)
    }

    @Test
    fun updateExpense_updatesLedgerWhenExpenseSwitchesFromBusinessToPersonal() = runBlocking {
        repository.saveExpense(
            Expense(
                expenseId = "switch-1",
                date = LocalDate.of(2026, 1, 15),
                title = "Fuel",
                amount = 12_500L,
                category = "Transport",
                isPersonal = false,
                note = "Original"
            )
        )

        val savedExpenseId = database.expenseDao().getAllExpensesForBackup().single().expenseId

        repository.updateExpense(
            Expense(
                expenseId = savedExpenseId,
                date = LocalDate.of(2026, 1, 16),
                title = "Home Fuel",
                amount = 9_000L,
                category = "Home",
                isPersonal = true,
                note = "Updated"
            )
        )

        val expense = database.expenseDao().getAllExpensesForBackup().single()
        assertEquals(savedExpenseId, expense.expenseId)
        assertEquals(LocalDate.of(2026, 1, 16).toMillis(), expense.dateMillis)
        assertEquals("Home Fuel", expense.title)
        assertEquals(9_000L, expense.amount)
        assertEquals(true, expense.isPersonal)

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(savedExpenseId, ledger.referenceId)
        assertEquals(OWNER_ACCOUNT_ID, ledger.accountId)
        assertEquals(LedgerEntryType.OWNER_DRAWING, ledger.type)
        assertEquals(9_000L, ledger.debit)
        assertEquals(0L, ledger.credit)
        assertEquals(0L, ledger.profitImpact)
        assertEquals("Home Fuel - Updated", ledger.note)
    }

    @Test
    fun deleteExpense_softDeletesExpenseAndRelatedLedger() = runBlocking {
        repository.saveExpense(
            Expense(
                expenseId = "delete-1",
                date = LocalDate.of(2026, 1, 15),
                title = "Fuel",
                amount = 12_500L,
                category = "Transport",
                isPersonal = false,
                note = null
            )
        )

        val expenseId = database.expenseDao().getAllExpensesForBackup().single().expenseId
        repository.deleteExpense(expenseId)

        val expense = database.expenseDao().getAllExpensesForBackup().single()
        assertNotNull(expense.deletedAtMillis)

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(expenseId, ledger.referenceId)
        assertNotNull(ledger.deletedAtMillis)
    }
}
