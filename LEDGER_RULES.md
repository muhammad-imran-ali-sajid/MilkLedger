# MilkLedger Ledger Rules

This document explains the financial meaning of MilkLedger's ledger rows.

It is intentionally practical. The goal is to help future refactors preserve business meaning, not to describe accounting theory in abstract terms.

## Core Mental Model

MilkLedger stores important money values in paisa as `Long`.

```text
Rs 1.00 = 100 paisa
Rs 500.00 = 50000 paisa
```

The app's account balance formula is:

```text
balance = SUM(debit) - SUM(credit)
```

So:

- positive balance means the account is on the debit side
- negative balance means the account is on the credit side
- deleted ledger rows are ignored by balance queries

## Ledger Fields

### `debit`

Money placed on the debit side of an account.

Current examples:

- customer milk sale bill increases customer receivable
- cash paid to supplier reduces supplier payable
- business expense records cash/expense outflow
- owner drawing records money taken by owner
- customer positive opening balance starts as debit

### `credit`

Money placed on the credit side of an account.

Current examples:

- customer cash received reduces customer receivable
- supplier milk purchase bill increases supplier payable
- supplier positive opening balance starts as credit
- customer negative opening balance starts as credit

### `profitImpact`

Money effect on business profit.

This is separate from account balance.

Current examples:

- milk sale increases profit: positive `profitImpact`
- milk purchase decreases profit: negative `profitImpact`
- business expense decreases profit: negative `profitImpact`
- cash paid/received has zero `profitImpact`
- owner drawing has zero `profitImpact`
- opening balance has zero `profitImpact`

## Account Opening Balance Rules

Opening balance creates one `OPENING_BALANCE` ledger row with:

```text
referenceId = accountId
type = OPENING_BALANCE
profitImpact = 0
note = "Opening Balance"
```

Customer:

```text
positive opening balance -> debit
negative opening balance -> credit
```

Supplier:

```text
positive opening balance -> credit
negative opening balance -> debit
```

Saving the same account again updates the existing opening balance ledger row. It should not create a duplicate opening balance row.

Deleting an account through `DeleteAccountUseCase` is allowed only when current balance is zero. If balance is not zero, deletion is blocked.

## Sale Rules

A milk sale creates a `MilkTransactionEntity` with:

```text
type = SALE
quantity = volume - deduction
totalAmount = (volume - deduction) * rate, converted to paisa
```

It also creates a `MILK_SALE` ledger row:

```text
debit = total sale amount
credit = 0
profitImpact = total sale amount
accountId = customer account id
referenceId = milk transaction id
```

If cash is received, it creates a `CASH_RECEIVED` ledger row:

```text
debit = 0
credit = amount received
profitImpact = 0
accountId = customer account id
referenceId = milk transaction id
```

Payment date is stored on the milk transaction as `paymentDateMillis`.

Accounting date for the cash ledger row remains the sale date.

If payment date differs from sale date, current note format is:

```text
Customer Name
(Dated: dd/MM)
```

## Purchase Rules

A milk purchase creates a `MilkTransactionEntity` with:

```text
type = PURCHASE
quantity = volume
ts = calculateTS(fat, lr, volume)
totalAmount = calculatePrice(volume, fat, lr, rate), converted to paisa
```

It also creates a `MILK_PURCHASE` ledger row:

```text
debit = 0
credit = total purchase amount
profitImpact = -total purchase amount
accountId = supplier account id
referenceId = milk transaction id
```

If cash is paid, it creates a `CASH_PAID` ledger row:

```text
debit = amount paid
credit = 0
profitImpact = 0
accountId = supplier account id
referenceId = milk transaction id
```

Payment date is stored on the milk transaction as `paymentDateMillis`.

Accounting date for the cash ledger row remains the purchase date.

If payment date differs from purchase date, current note format is:

```text
Supplier Name
(Dated: dd/MM)
```

Known current behavior: quality purchase totals can truncate by one paisa because the app calculates using `Double` and then calls `toLong()`.

Do not change this casually. If we fix rounding, do it as an explicit refactor with tests and migration/compatibility thinking.

## Expense Rules

Expenses have two meanings.

### Business Expense

Business expense means shop/business cost.

It creates a ledger row:

```text
accountId = SHOP_EXPENSE
type = BUSINESS_EXPENSE
debit = expense amount
credit = 0
profitImpact = -expense amount
referenceId = expense id
```

Business expense reduces business profit.

### Personal Expense

Personal expense means owner drawing.

It creates a ledger row:

```text
accountId = OWNER_001
type = OWNER_DRAWING
debit = expense amount
credit = 0
profitImpact = 0
referenceId = expense id
```

Personal expense does not reduce business profit.

### System Accounts

Before saving expenses, the repository ensures these system accounts exist:

```text
SHOP_EXPENSE
OWNER_001
```

These are internal accounts used for ledger consistency.

## Soft Delete Rules

Sale delete:

```text
soft-delete milk transaction
soft-delete all ledger rows with referenceId = sale id
```

Purchase delete:

```text
soft-delete milk transaction
soft-delete all ledger rows with referenceId = purchase id
```

Expense delete:

```text
soft-delete expense
soft-delete ledger row with referenceId = expense id
```

Account delete:

```text
allowed only when balance is zero
soft-delete account
soft-delete opening balance row
```

## Refactor Rule

When changing ledger code, ask:

1. Which user action happened?
2. Which domain record should be created or updated?
3. Which ledger row or rows should exist?
4. Which side is debit?
5. Which side is credit?
6. Does this affect business profit?
7. Which rows are soft-deleted on delete?
8. Which test proves this behavior?

If you cannot answer those questions, do not refactor that path yet.

