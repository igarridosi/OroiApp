package com.example.oroiapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.oroiapp.data.ThemeSetting
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale.filter

enum class SubscriptionFilter {
    ALFABETIKOA,
    ORDAINKETA_DATA,
    PREZIOA
}

// Interfazearen egoera definitzen du
data class MainUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val totalMonthlyCost: Double = 0.0,
    val totalAnnualCost: Double = 0.0,
    val totalDailyCost: Double = 0.0,
    val username: String = "",
    val showUsernameDialog: Boolean = false,
    val currentTheme: ThemeSetting = ThemeSetting.SYSTEM,
    val currentFilter: SubscriptionFilter = SubscriptionFilter.ALFABETIKOA
)

private data class AllCosts(
    val monthly: Double,
    val annual: Double,
    val daily: Double
)

class MainViewModel(
    private val subscriptionDao: SubscriptionDao,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    // Sortu MutableStateFlow bat uneko gaia gordetzeko ViewModel-ean.
    // Hasierako balioa SharedPreferences-etik irakurtzen dugu.
    private val _currentTheme = MutableStateFlow(userPrefs.getThemeSetting())

    // StateFlow bat interfazearen egoera erakusteko
    private val _username = MutableStateFlow(userPrefs.getUsername())
    private val _showUsernameDialog = MutableStateFlow(userPrefs.isFirstLaunch())
    private val _dialogUsernameInput = MutableStateFlow("")
    val dialogUsernameInput: StateFlow<String> = _dialogUsernameInput.asStateFlow()
    private val _currentFilter = MutableStateFlow(SubscriptionFilter.ALFABETIKOA)

    val uiState: StateFlow<MainUiState> = combine(
        subscriptionDao.getAllSubscriptions(),
        _username,
        _showUsernameDialog,
        _currentTheme,
        _currentFilter
    ) { subs, name, showDialog, theme, filter ->
        val sortedSubs = sortSubscriptions(subs, filter)
        val allCosts = calculateAllCosts(subs)
        MainUiState(
            subscriptions = sortedSubs,
            totalMonthlyCost = allCosts.monthly,
            totalAnnualCost = allCosts.annual,
            totalDailyCost = allCosts.daily,
            username = name,
            showUsernameDialog = showDialog,
            currentTheme = theme,
            currentFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainUiState()
    )

    // Funtzio nagusi bat kostu guztiak kalkulatzeko
    private fun calculateAllCosts(subscriptions: List<Subscription>): AllCosts {
        val monthlyCost = subscriptions.sumOf { sub ->
            when (sub.billingCycle) {
                BillingCycle.WEEKLY -> sub.amount * 4 // Hurbilketa
                BillingCycle.MONTHLY -> sub.amount
                BillingCycle.ANNUAL -> sub.amount / 12
            }
        }
        val annualCost = monthlyCost * 12
        val dailyCost = monthlyCost / 30 // Hurbilketa (30 eguneko hilabetea)

        return AllCosts(monthly = monthlyCost, annual = annualCost, daily = dailyCost)
    }

    fun changeTheme(newTheme: ThemeSetting) {
        userPrefs.saveThemeSetting(newTheme) // Gorde SharedPreferences-en
        _currentTheme.value = newTheme       // Eguneratu ViewModel-eko egoera
    }

    fun onDialogUsernameChange(name: String) {
        _dialogUsernameInput.value = name
    }

    fun onUsernameSave() {
        val name = _dialogUsernameInput.value.trim()
        if (name.isNotBlank()) {
            userPrefs.saveUsername(name)
            _username.value = name
            _showUsernameDialog.value = false
        }
    }

    fun updateFilter(filter: SubscriptionFilter) {
        _currentFilter.value = filter
    }

    private fun sortSubscriptions(subscriptions: List<Subscription>, filter: SubscriptionFilter): List<Subscription> {
        return when (filter) {
            SubscriptionFilter.ALFABETIKOA -> subscriptions.sortedBy { it.name }
            SubscriptionFilter.ORDAINKETA_DATA -> subscriptions.sortedBy { calculateNextPaymentDate(it) }
            SubscriptionFilter.PREZIOA -> subscriptions.sortedByDescending { it.amount }
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        val today = java.util.Calendar.getInstance()
        today.set(java.util.Calendar.HOUR_OF_DAY, 0)
        today.set(java.util.Calendar.MINUTE, 0)
        today.set(java.util.Calendar.SECOND, 0)
        today.set(java.util.Calendar.MILLISECOND, 0)

        calendar.time = subscription.firstPaymentDate

        if (calendar.time.after(today.time)) {
            return calendar.time
        }

        while (calendar.time.before(today.time)) {
            when (subscription.billingCycle) {
                BillingCycle.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
                BillingCycle.ANNUAL -> calendar.add(java.util.Calendar.YEAR, 1)
            }
        }

        return calendar.time
    }
}