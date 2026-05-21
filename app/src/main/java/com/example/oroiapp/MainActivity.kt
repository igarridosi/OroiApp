package com.example.oroiapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.ui.AddSubscriptionScreen
import com.example.oroiapp.ui.EditSubscriptionScreen
import com.example.oroiapp.ui.MainScreen
import com.example.oroiapp.ui.StatisticsScreen
import com.example.oroiapp.ui.theme.OroiTheme
import com.example.oroiapp.viewmodel.AddEditViewModel
import com.example.oroiapp.viewmodel.EditSubscriptionViewModel
import com.example.oroiapp.viewmodel.MainViewModel
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val messageRes = if (isGranted) R.string.notifications_enabled else R.string.notifications_denied
        Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    override fun attachBaseContext(newBase: Context) {
        val tag = newBase.getSharedPreferences(UserPreferencesRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(UserPreferencesRepository.LANGUAGE_TAG_KEY, "") ?: ""
        if (tag.isEmpty()) {
            super.attachBaseContext(newBase)
        } else {
            val locale = Locale.forLanguageTag(tag)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Suppress enter animation on Activity recreation (language change)
        overridePendingTransition(0, 0)
        askNotificationPermission()

        val mainViewModel = ViewModelProvider(this, OroiViewModelFactory)[MainViewModel::class.java]

        // Observe language change: recreate instantly with no transition flash
        lifecycleScope.launch {
            mainViewModel.languageChangeEvent.collect {
                recreate()
                overridePendingTransition(0, 0)
            }
        }

        setContent {
            val uiState by mainViewModel.uiState.collectAsState()

            OroiTheme(themeSetting = uiState.currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OroiApp(OroiViewModelFactory)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun OroiApp(factory: ViewModelProvider.Factory) {
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(
                viewModel = mainViewModel,
                onAddSubscription = { navController.navigate("add_subscription") },
                onEditSubscription = { subscriptionId -> navController.navigate("edit_subscription/$subscriptionId") },
                onCancelSubscription = { subscription ->
                    scope.launch {
                        val dao = OroiViewModelFactory.cancellationDao
                        val link = dao.findLinkByName("%${subscription.name}%")
                        if (link != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.cancel_link_not_found, subscription.name),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                onStatsClick = { navController.navigate("statistics_screen") }
            )
        }

        composable(route = "add_subscription") {
            val addViewModel: AddEditViewModel = viewModel(factory = factory)
            AddSubscriptionScreen(
                viewModel = addViewModel,
                onNavigateBack = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "edit_subscription/{subscriptionId}",
            arguments = listOf(navArgument("subscriptionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getInt("subscriptionId")
            val editViewModel: EditSubscriptionViewModel = viewModel(
                key = subscriptionId?.toString(),
                factory = factory
            )
            EditSubscriptionScreen(
                viewModel = editViewModel,
                onNavigateBack = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }
            )
        }

        composable("statistics_screen") {
            StatisticsScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
