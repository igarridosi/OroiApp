package com.example.oroiapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oroiapp.data.CancellationLinkDao
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.worker.NotificationScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// Formularioko eremuen egoera gordetzeko datu-klasea
data class SubscriptionFormState(
    val name: String = "",
    val amount: String = "",
    val currency: String = "EUR",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val firstPaymentDate: Date = Date() // Gaurko data lehenetsi bezala
)

class AddEditViewModel(
    private val application: Application,
    private val subscriptionDao: SubscriptionDao,
    private val cancellationLinkDao: CancellationLinkDao
) : ViewModel() {

    private val _formState = MutableStateFlow(SubscriptionFormState())
    val formState = _formState.asStateFlow()

    private val _navigationChannel = Channel<Unit>()
    val navigationEvent = _navigationChannel.receiveAsFlow()

    val predefinedServiceNames: StateFlow<List<String>> =
        cancellationLinkDao.getAllServiceNames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSubscription() {
        viewModelScope.launch {
            val state = _formState.value
            if (state.name.isBlank() || state.amount.isBlank()) return@launch

            val newSubscription = Subscription(
                id = 0,
                name = state.name,
                amount = state.amount.toDouble(),
                currency = state.currency,
                billingCycle = state.billingCycle,
                firstPaymentDate = state.firstPaymentDate
            )
            // ZUZENDUTA: 'add' deitzen dugu, ez 'insert'
            subscriptionDao.add(newSubscription)
            NotificationScheduler.scheduleReminder(application.applicationContext, newSubscription)
            _navigationChannel.send(Unit)
        }
    }

    fun onNameChange(newName: String) { _formState.update { it.copy(name = newName) } }
    fun onAmountChange(newAmount: String) { if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d*\$"))) { _formState.update { it.copy(amount = newAmount) } } }
    fun onBillingCycleChange(newCycle: BillingCycle) { _formState.update { it.copy(billingCycle = newCycle) } }
    fun onDateChange(newDate: Date) { _formState.update { it.copy(firstPaymentDate = newDate) } }
}