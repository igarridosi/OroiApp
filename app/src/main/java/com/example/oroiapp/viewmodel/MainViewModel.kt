package com.example.oroiapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.data.ThemeSetting
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SubscriptionFilter {
    ALFABETIKOA,
    ORDAINKETA_DATA,
    PREZIOA
}

data class MainUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val totalMonthlyCost: Double = 0.0,
    val totalAnnualCost: Double = 0.0,
    val totalDailyCost: Double = 0.0,
    val username: String = "",
    val showUsernameDialog: Boolean = false,
    val currentTheme: ThemeSetting = ThemeSetting.SYSTEM,
    val currentFilter: SubscriptionFilter = SubscriptionFilter.ALFABETIKOA,
    val monthlyBudget: Double = 0.0,
    val searchQuery: String = ""
)

data class ChartData(
    val label: String,
    val value: Float
)

private data class AllCosts(val monthly: Double, val annual: Double, val daily: Double)

class MainViewModel(
    private val subscriptionDao: SubscriptionDao,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(userPrefs.getThemeSetting())
    private val _username = MutableStateFlow(userPrefs.getUsername())
    private val _showUsernameDialog = MutableStateFlow(userPrefs.isFirstLaunch())
    private val _dialogUsernameInput = MutableStateFlow("")
    val dialogUsernameInput: StateFlow<String> = _dialogUsernameInput.asStateFlow()
    private val _currentFilter = MutableStateFlow(SubscriptionFilter.ALFABETIKOA)
    private val _monthlyBudget = MutableStateFlow(userPrefs.getMonthlyBudget())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        listOf(
            subscriptionDao.getAllSubscriptions(),
            _username,
            _showUsernameDialog,
            _currentTheme,
            _currentFilter,
            _monthlyBudget,
            _searchQuery
        )
    ) { args ->
        val subs = args[0] as List<Subscription>
        val name = args[1] as String
        val showDialog = args[2] as Boolean
        val theme = args[3] as ThemeSetting
        val filter = args[4] as SubscriptionFilter
        val budget = args[5] as Double
        val query = args[6] as String
        val filtered = if (query.isBlank()) subs
                       else subs.filter { it.name.contains(query, ignoreCase = true) }
        val allCosts = calculateAllCosts(filtered)
        MainUiState(
            subscriptions = sortSubscriptions(filtered, filter),
            totalMonthlyCost = allCosts.monthly,
            totalAnnualCost = allCosts.annual,
            totalDailyCost = allCosts.daily,
            username = name,
            showUsernameDialog = showDialog,
            currentTheme = theme,
            monthlyBudget = budget,
            currentFilter = filter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainUiState()
    )

    private fun calculateAllCosts(subscriptions: List<Subscription>): AllCosts {
        val monthlyCost = subscriptions.sumOf { sub ->
            when (sub.billingCycle) {
                BillingCycle.WEEKLY -> sub.amount * 4
                BillingCycle.MONTHLY -> sub.amount
                BillingCycle.ANNUAL -> sub.amount / 12
            }
        }
        return AllCosts(monthly = monthlyCost, annual = monthlyCost * 12, daily = monthlyCost / 30)
    }

    fun changeTheme(newTheme: ThemeSetting) {
        userPrefs.saveThemeSetting(newTheme)
        _currentTheme.value = newTheme
    }

    fun onDialogUsernameChange(name: String) { _dialogUsernameInput.value = name }

    fun onUsernameSave() {
        val name = _dialogUsernameInput.value.trim()
        if (name.isNotBlank()) {
            userPrefs.saveUsername(name)
            _username.value = name
            _showUsernameDialog.value = false
        }
    }

    fun updateUsername(name: String) {
        if (name.isNotBlank()) {
            userPrefs.saveUsername(name)
            _username.value = name
        }
    }

    private val _languageChangeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val languageChangeEvent: SharedFlow<Unit> = _languageChangeEvent.asSharedFlow()

    fun changeLanguage(tag: String) {
        userPrefs.saveLanguageTag(tag)
        _languageChangeEvent.tryEmit(Unit)
    }

    fun getCurrentLanguageTag(): String = userPrefs.getLanguageTag()

    fun updateFilter(filter: SubscriptionFilter) { _currentFilter.value = filter }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    private fun sortSubscriptions(subscriptions: List<Subscription>, filter: SubscriptionFilter): List<Subscription> {
        return when (filter) {
            SubscriptionFilter.ALFABETIKOA -> subscriptions.sortedBy { it.name }
            SubscriptionFilter.ORDAINKETA_DATA -> subscriptions.sortedBy { calculateNextPaymentDate(it) }
            SubscriptionFilter.PREZIOA -> subscriptions.sortedByDescending { it.amount }
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        calendar.time = subscription.firstPaymentDate
        if (calendar.time.after(today.time)) return calendar.time
        while (calendar.time.before(today.time)) {
            when (subscription.billingCycle) {
                BillingCycle.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
                BillingCycle.ANNUAL -> calendar.add(java.util.Calendar.YEAR, 1)
            }
        }
        return calendar.time
    }

    fun onBudgetChange(newBudget: Double) {
        userPrefs.saveMonthlyBudget(newBudget)
        _monthlyBudget.value = newBudget
    }

    suspend fun exportToCsv(context: Context): Uri? {
        return try {
            val subs = subscriptionDao.getAllSubscriptions().first()
            if (subs.isEmpty()) return null
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val csv = buildString {
                appendLine("Name,Amount,Currency,Billing Cycle,First Payment Date")
                subs.forEach { sub ->
                    appendLine("\"${sub.name}\",${sub.amount},${sub.currency},${sub.billingCycle.name},${dateFormat.format(sub.firstPaymentDate)}")
                }
            }
            val file = File(context.cacheDir, "oroi_subscriptions.csv")
            file.writeText(csv)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            null
        }
    }

    val allExpensesChartData: Flow<List<ChartData>> = uiState.map { state ->
        state.subscriptions
            .map { sub ->
                val monthlyValue = when (sub.billingCycle) {
                    BillingCycle.WEEKLY -> sub.amount * 4
                    BillingCycle.MONTHLY -> sub.amount
                    BillingCycle.ANNUAL -> sub.amount / 12
                }
                sub to monthlyValue
            }
            .sortedByDescending { it.second }
            .map { (sub, monthlyValue) -> ChartData(label = sub.name, value = monthlyValue.toFloat()) }
    }
}
