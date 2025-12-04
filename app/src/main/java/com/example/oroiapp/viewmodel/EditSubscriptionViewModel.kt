package com.example.oroiapp.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.oroiapp.worker.NotificationScheduler

class EditSubscriptionViewModel(
    private val subscriptionDao: SubscriptionDao,
    private val context: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(SubscriptionFormState())
    val formState = _formState.asStateFlow()

    private val editingSubscriptionId: Int = checkNotNull(savedStateHandle["subscriptionId"])

    init {
        loadSubscriptionData()
    }

    private fun loadSubscriptionData() {
        viewModelScope.launch {
            val subscription = subscriptionDao.getSubscriptionById(editingSubscriptionId)
            if (subscription != null) {
                _formState.value = SubscriptionFormState(
                    name = subscription.name,
                    amount = subscription.amount.toString(),
                    currency = subscription.currency,
                    billingCycle = subscription.billingCycle,
                    firstPaymentDate = subscription.firstPaymentDate
                )
            }
        }
    }

    suspend fun saveSubscription() {
        val state = _formState.value
        if (state.name.isBlank() || state.amount.isBlank()) return

        val updatedSubscription = Subscription(
            id = editingSubscriptionId,
            name = state.name,
            amount = state.amount.toDouble(),
            currency = state.currency,
            billingCycle = state.billingCycle,
            firstPaymentDate = state.firstPaymentDate
        )

        subscriptionDao.update(updatedSubscription)
        NotificationScheduler.scheduleReminder(context, updatedSubscription)
    }

    suspend fun deleteSubscription() {
        val subscriptionToDelete = Subscription(
            id = editingSubscriptionId,
            name = "",
            amount = 0.0,
            currency = "",
            billingCycle = BillingCycle.MONTHLY,
            firstPaymentDate = Date()
        )

        subscriptionDao.delete(subscriptionToDelete)
        NotificationScheduler.cancelReminder(context, editingSubscriptionId)
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

