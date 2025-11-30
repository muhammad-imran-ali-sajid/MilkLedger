package com.miassolutions.milkledger.presentation.expenses

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections.emptyList

private const val KEY_EXPENSES = "expenses_list"

fun Context.saveExpenseList(list: List<String>) {
    val json = Gson().toJson(list)
    getSharedPreferences("expense_prefs", Context.MODE_PRIVATE).edit {
        putString(KEY_EXPENSES, json)
    }
}


fun Context.loadStringList(): List<String> {
    val prefs = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString(KEY_EXPENSES, null) ?: return emptyList()

    val type = object : TypeToken<List<String>>() {}.type
    return Gson().fromJson(json, type)
}