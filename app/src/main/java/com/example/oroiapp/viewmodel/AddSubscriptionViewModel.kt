package com.example.oroiapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oroiapp.data.CancellationLinkDao
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.widget.OroiWidget
import com.example.oroiapp.worker.NotificationScheduler
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Date

data class SubscriptionFormState(
    val name: String = "",
    val amount: String = "",
    val currency: String = "EUR",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val firstPaymentDate: Date = Date()
)

class AddEditViewModel(
    private val application: Application,
    private val subscriptionDao: SubscriptionDao,
    private val cancellationLinkDao: CancellationLinkDao
) : ViewModel() {

    private val _formState = MutableStateFlow(SubscriptionFormState())
    val formState = _formState.asStateFlow()

    val predefinedServiceNames: StateFlow<List<String>> =
        cancellationLinkDao.getAllServiceNames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun saveSubscription(): Boolean {
        val state = _formState.value
        val amount = state.amount.toDoubleOrNull() ?: return false
        if (state.name.isBlank() || amount <= 0) return false
        return try {
            val newSubscription = Subscription(
                id = 0,
                name = state.name,
                amount = amount,
                currency = state.currency,
                billingCycle = state.billingCycle,
                firstPaymentDate = state.firstPaymentDate
            )
            // add() now returns the real auto-generated row ID
            val insertedId = subscriptionDao.add(newSubscription)
            val savedSubscription = newSubscription.copy(id = insertedId.toInt())
            NotificationScheduler.scheduleReminder(application.applicationContext, savedSubscription)
            OroiWidget().updateAll(application.applicationContext)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun onNameChange(newName: String) { _formState.update { it.copy(name = newName) } }
    fun onAmountChange(newAmount: String) {
        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _formState.update { it.copy(amount = newAmount) }
        }
    }
    fun onBillingCycleChange(newCycle: BillingCycle) { _formState.update { it.copy(billingCycle = newCycle) } }
    fun onDateChange(newDate: Date) { _formState.update { it.copy(firstPaymentDate = newDate) } }
}
