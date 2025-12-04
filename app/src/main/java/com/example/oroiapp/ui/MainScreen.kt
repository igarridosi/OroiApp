package com.example.oroiapp.ui

import android.R
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oroiapp.data.ThemeSetting
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.ui.theme.OroiTheme
import com.example.oroiapp.viewmodel.MainUiState
import com.example.oroiapp.viewmodel.MainViewModel
import com.example.oroiapp.viewmodel.SubscriptionFilter
import java.text.SimpleDateFormat
import java.util.*

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
                text = "Ongi Etorri, ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
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
    onCancelSubscription: (Subscription) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogInput by viewModel.dialogUsernameInput.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeChooserDialog(
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { newTheme ->
                viewModel.changeTheme(newTheme)
                showThemeDialog = false
            }
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
                    .padding(top = 16.dp, bottom = 70.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showThemeDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val icon = when (uiState.currentTheme) {
                        ThemeSetting.LIGHT -> Icons.Default.LightMode
                        ThemeSetting.DARK -> Icons.Default.DarkMode
                        ThemeSetting.SYSTEM -> Icons.Default.SettingsBrightness
                    }
                    Icon(icon, contentDescription = "Aldatu Gaia", tint = MaterialTheme.colorScheme.surface)
                }

                FloatingActionButton(
                    onClick = onAddSubscription,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Gehitu Harpidetza", tint = MaterialTheme.colorScheme.surface)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        )   {
            // Usamos el nuevo header
            MainHeader(username = uiState.username)
            Spacer(modifier = Modifier.height(24.dp))
            CostCarousel(uiState = uiState)
            Spacer(modifier = Modifier.height(24.dp))
            FilterChipRow(
                currentFilter = uiState.currentFilter,
                onFilterSelected = viewModel::updateFilter
            )
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
                0 -> CostCard(title = "Hileko Gastua", amount = uiState.totalMonthlyCost)
                1 -> CostCard(title = "Urteko Gastua", amount = uiState.totalAnnualCost)
                2 -> CostCard(title = "Eguneko Gastua", amount = uiState.totalDailyCost)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
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
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.surface

            )
            Text(
                text = "€${"%.2f".format(amount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun FilterChipRow(
    currentFilter: SubscriptionFilter,
    onFilterSelected: (SubscriptionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == SubscriptionFilter.ALFABETIKOA,
            onClick = { onFilterSelected(SubscriptionFilter.ALFABETIKOA) },
            label = { Text("Alfabetikoa") }
        )
        FilterChip(
            selected = currentFilter == SubscriptionFilter.ORDAINKETA_DATA,
            onClick = { onFilterSelected(SubscriptionFilter.ORDAINKETA_DATA) },
            label = { Text("Ordainketa") }
        )
        FilterChip(
            selected = currentFilter == SubscriptionFilter.PREZIOA,
            onClick = { onFilterSelected(SubscriptionFilter.PREZIOA) },
            label = { Text("Prezioa") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionList(
    subscriptions: List<Subscription>,
    onEdit: (Int) -> Unit,
    onCancel: (Subscription) -> Unit,
    contentPadding: PaddingValues
) {
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
                    contentDescription = "Ez dago harpidetzarik",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gehitu zure Harpidetzak '+' ikonoan",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            // Padding-a elementu bakoitzari emango diogu, ez zerrendari
            verticalArrangement = Arrangement.spacedBy(8.dp),

        ) {
            items(items = subscriptions, key = { it.id }) { subscription ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            // Eskuinetik ezkerrera -> Editatu
                            SwipeToDismissBoxValue.EndToStart -> {
                                onEdit(subscription.id)
                                return@rememberSwipeToDismissBoxState false // Ez dugu elementua desagertzerik nahi
                            }
                            // Ezkerretik eskuinera -> Ezeztatu ("Botoi Gorria")
                            SwipeToDismissBoxValue.StartToEnd -> {
                                onCancel(subscription)
                                return@rememberSwipeToDismissBoxState false // Ez dugu elementua desagertzerik nahi
                            }

                            else -> return@rememberSwipeToDismissBoxState false
                        }
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.padding(vertical = 4.dp),
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismissBox

                        // Definitu aldagaiak norabidearen arabera, modu argiagoan
                        val backgroundColor: Color
                        val icon: ImageVector
                        val alignment: Alignment
                        val tintColor: Color

                        when (direction) {
                            // "Botoi Gorria" (Ezeztatu)
                            SwipeToDismissBoxValue.StartToEnd -> {
                                backgroundColor = MaterialTheme.colorScheme.error
                                icon = Icons.Default.AdsClick
                                alignment = Alignment.CenterStart
                                tintColor = MaterialTheme.colorScheme.onError
                            }
                            // Editatzeko ekintza
                            SwipeToDismissBoxValue.EndToStart -> {
                                backgroundColor = MaterialTheme.colorScheme.onPrimary
                                icon = Icons.Default.Edit
                                alignment = Alignment.CenterEnd
                                tintColor = MaterialTheme.colorScheme.surface
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
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = tintColor
                            )
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

    // Box-a kendu eta Card-a da elementu nagusia
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box { // Box hau badge-a kokatzeko da
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(subscription.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Hurrengo ordainketa: ${dateFormat.format(nextPaymentDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(text = "${subscription.amount} ${subscription.currency}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            BillingCycleBadge(
                cycle = subscription.billingCycle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp) // Padding-a badge-ari
            )
        }
    }
}

@Composable
fun BillingCycleBadge(cycle: BillingCycle, modifier: Modifier = Modifier) {
    val (text, color) = when (cycle) {
        BillingCycle.WEEKLY -> Triple("A", MaterialTheme.colorScheme.onTertiary, MaterialTheme.colorScheme.onTertiaryContainer)
        BillingCycle.MONTHLY -> Triple("H", MaterialTheme.colorScheme.onTertiaryContainer, MaterialTheme.colorScheme.onPrimary)
        BillingCycle.ANNUAL -> Triple("U", MaterialTheme.colorScheme.onSecondary, MaterialTheme.colorScheme.onSecondary)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
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
        title = { Text("Aukeratu Gaia") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { onThemeSelected(ThemeSetting.SYSTEM) }) {
                    Text("Sistemaren arabera")
                }
                TextButton(onClick = { onThemeSelected(ThemeSetting.LIGHT) }) {
                    Text("Modu Argia")
                }
                TextButton(onClick = { onThemeSelected(ThemeSetting.DARK) }) {
                    Text("Modu Iluna")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Itxi")
                }
            }
        }
    )
}