package com.miassolutions.milkledger.core.activities

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class AppStartViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    val startDestination = flow {
        val owner = repository.getOwner()
        emit(
            if (owner == null)
                R.id.ownerSetupFragment
            else
                R.id.dashboardFragment
        )
    }
}