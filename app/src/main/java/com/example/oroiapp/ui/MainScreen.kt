package com.example.oroiapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.example.oroiapp.R
import com.example.oroiapp.data.ThemeSetting
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.viewmodel.MainUiState
import com.example.oroiapp.viewmodel.MainViewModel
import com.example.oroiapp.viewmodel.SubscriptionFilter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.rememberLazyListState


@Composable
fun MainHeader(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "oroi",
            fontSize = 32.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.welcome_greeting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddSubscription: () -> Unit,
    onEditSubscription: (Int) -> Unit,
    onCancelSubscription: (Subscription) -> Unit,
    onStatsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogInput by viewModel.dialogUsernameInput.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showSettingsSheet) {
        SettingsBottomSheet(
            uiState = uiState,
            currentLanguageTag = viewModel.getCurrentLanguageTag(),
            onThemeSelected = viewModel::changeTheme,
            onLanguageSelected = viewModel::changeLanguage,
            onUsernameUpdated = viewModel::updateUsername,
            onExportCsv = {
                showSettingsSheet = false
                scope.launch {
                    val uri = viewModel.exportToCsv(context)
                    if (uri != null) {
                        val shareIntent = android.content.Intent.createChooser(
                            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            context.getString(R.string.export_csv_share_title)
                        )
                        context.startActivity(shareIntent)
                    } else {
                        android.widget.Toast.makeText(
                            context, context.getString(R.string.export_csv_empty), android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (uiState.showUsernameDialog) {
        UsernamePromptDialog(
            currentInput = dialogInput,
            onInputChange = viewModel::onDialogUsernameChange,
            onSave = viewModel::onUsernameSave
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showSettingsSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.fab_settings),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                FloatingActionButton(
                    onClick = onAddSubscription,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.fab_add_subscription),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            MainHeader(username = uiState.username)
            Spacer(modifier = Modifier.height(24.dp))
            CostCarousel(uiState = uiState)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetProgressBar(
                currentMonthlyCost = uiState.totalMonthlyCost,
                budgetLimit = uiState.monthlyBudget,
                onBudgetChange = viewModel::onBudgetChange
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilterChipRow(
                currentFilter = uiState.currentFilter,
                onFilterSelected = viewModel::updateFilter,
                onStatsClick = onStatsClick,
                showSearch = showSearch,
                onSearchToggle = {
                    showSearch = !showSearch
                    if (!showSearch) viewModel.updateSearchQuery("")
                }
            )
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SubscriptionSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            SubscriptionList(
                subscriptions = uiState.subscriptions,
                onEdit = onEditSubscription,
                onCancel = onCancelSubscription,
                contentPadding = paddingValues
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CostCarousel(uiState: MainUiState) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp,
        ) { page ->
            when (page) {
                0 -> CostCard(title = stringResource(R.string.carousel_monthly_cost), amount = uiState.totalMonthlyCost)
                1 -> CostCard(title = stringResource(R.string.carousel_annual_cost), amount = uiState.totalAnnualCost)
                2 -> CostCard(title = stringResource(R.string.carousel_daily_cost), amount = uiState.totalDailyCost)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun CostCard(title: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "€${"%.2f".format(amount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubscriptionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear_description))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilterChipRow(
    currentFilter: SubscriptionFilter,
    onFilterSelected: (SubscriptionFilter) -> Unit,
    onStatsClick: () -> Unit,
    showSearch: Boolean = false,
    onSearchToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = currentFilter == SubscriptionFilter.ALFABETIKOA,
            onClick = { onFilterSelected(SubscriptionFilter.ALFABETIKOA) },
            label = { Text(stringResource(R.string.filter_alphabetical)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        FilterChip(
            selected = currentFilter == SubscriptionFilter.ORDAINKETA_DATA,
            onClick = { onFilterSelected(SubscriptionFilter.ORDAINKETA_DATA) },
            label = { Text(stringResource(R.string.filter_date)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        FilterChip(
            selected = currentFilter == SubscriptionFilter.PREZIOA,
            onClick = { onFilterSelected(SubscriptionFilter.PREZIOA) },
            label = { Text(stringResource(R.string.filter_price)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onSearchToggle,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (showSearch) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_hint),
                tint = if (showSearch) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(0.2f))
        IconButton(
            onClick = onStatsClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = stringResource(R.string.stats_button_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubscriptionList(
    subscriptions: List<Subscription>,
    onEdit: (Int) -> Unit,
    onCancel: (Subscription) -> Unit,
    contentPadding: PaddingValues
) {
    var pendingCancel by remember { mutableStateOf<Subscription?>(null) }

    pendingCancel?.let { sub ->
        AlertDialog(
            onDismissRequest = { pendingCancel = null },
            title = { Text(stringResource(R.string.confirm_cancel_title)) },
            text = { Text(stringResource(R.string.confirm_cancel_message, sub.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onCancel(sub)
                    pendingCancel = null
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancel = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (subscriptions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = stringResource(R.string.empty_state_icon_description),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.empty_state_text),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val listState = rememberLazyListState()

        LaunchedEffect(subscriptions) {
            listState.scrollToItem(0)
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items = subscriptions, key = { it.id }) { subscription ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                onEdit(subscription.id)
                                return@rememberSwipeToDismissBoxState false
                            }
                            SwipeToDismissBoxValue.StartToEnd -> {
                                pendingCancel = subscription
                                return@rememberSwipeToDismissBoxState false
                            }
                            else -> return@rememberSwipeToDismissBoxState false
                        }
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem(),
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismissBox

                        val backgroundColor: Color
                        val icon: ImageVector
                        val alignment: Alignment
                        val tintColor: Color

                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                backgroundColor = MaterialTheme.colorScheme.error
                                icon = Icons.Default.AdsClick
                                alignment = Alignment.CenterStart
                                tintColor = MaterialTheme.colorScheme.onError
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                backgroundColor = MaterialTheme.colorScheme.primary
                                icon = Icons.Default.Edit
                                alignment = Alignment.CenterEnd
                                tintColor = MaterialTheme.colorScheme.onPrimary
                            }
                            else -> {
                                backgroundColor = Color.Transparent
                                icon = Icons.Default.Delete
                                alignment = Alignment.Center
                                tintColor = Color.Transparent
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(icon, contentDescription = null, tint = tintColor)
                        }
                    }
                ) {
                    SubscriptionItem(subscription = subscription)
                }
            }
        }
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    val nextPaymentDate = calculateNextPaymentDate(subscription)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        subscription.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.next_payment, dateFormat.format(nextPaymentDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${subscription.amount} ${subscription.currency}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BillingCycleBadge(
                cycle = subscription.billingCycle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun BillingCycleBadge(cycle: BillingCycle, modifier: Modifier = Modifier) {
    val text = when (cycle) {
        BillingCycle.WEEKLY -> stringResource(R.string.billing_badge_weekly)
        BillingCycle.MONTHLY -> stringResource(R.string.billing_badge_monthly)
        BillingCycle.ANNUAL -> stringResource(R.string.billing_badge_annual)
    }
    val description = when (cycle) {
        BillingCycle.WEEKLY -> stringResource(R.string.billing_badge_weekly_desc)
        BillingCycle.MONTHLY -> stringResource(R.string.billing_badge_monthly_desc)
        BillingCycle.ANNUAL -> stringResource(R.string.billing_badge_annual_desc)
    }
    val color = when (cycle) {
        BillingCycle.WEEKLY -> MaterialTheme.colorScheme.tertiary
        BillingCycle.MONTHLY -> MaterialTheme.colorScheme.primary
        BillingCycle.ANNUAL -> MaterialTheme.colorScheme.secondary
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .semantics { contentDescription = description }
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

private fun calculateNextPaymentDate(subscription: Subscription): Date {
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)
    calendar.time = subscription.firstPaymentDate
    if (calendar.time.after(today.time)) {
        return calendar.time
    }
    while (calendar.time.before(today.time)) {
        when (subscription.billingCycle) {
            BillingCycle.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BillingCycle.ANNUAL -> calendar.add(Calendar.YEAR, 1)
        }
    }
    return calendar.time
}

@Composable
fun ThemeChooserDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_choose_theme)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { onThemeSelected(ThemeSetting.SYSTEM) }) {
                    Text(stringResource(R.string.theme_system), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.SettingsBrightness,
                        contentDescription = stringResource(R.string.theme_system_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = { onThemeSelected(ThemeSetting.LIGHT) }) {
                    Text(stringResource(R.string.theme_light), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = stringResource(R.string.theme_light_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = { onThemeSelected(ThemeSetting.DARK) }) {
                    Text(stringResource(R.string.theme_dark), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = stringResource(R.string.theme_dark_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}
