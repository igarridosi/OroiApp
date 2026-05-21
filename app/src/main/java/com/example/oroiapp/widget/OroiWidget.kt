package com.example.oroiapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.oroiapp.MainActivity
import com.example.oroiapp.R
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.text.FontStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.delay

val IsLoadingKey = booleanPreferencesKey("is_loading")

class OroiWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val isLoading = prefs[IsLoadingKey] ?: false

        val dao = OroiViewModelFactory.dao
        val allSubs = try {
            dao.getAllSubscriptions().first()
        } catch (e: Exception) {
            emptyList()
        }

        // Show all billing cycles sorted by days until next payment, top 4
        val targetSubs = allSubs
            .map { sub ->
                val nextDate = calculateNextPayment(sub)
                val daysLeft = calculateDaysLeft(nextDate)
                Triple(sub, nextDate, daysLeft)
            }
            .sortedBy { it.third }
            .take(4)

        // Localised strings from saved language preference
        val localCtx = getLocalizedContext(context)
        val emptyText = localCtx.getString(R.string.widget_empty_text)

        provideContent {
            GlanceTheme {
                WidgetContent(targetSubs, isLoading, emptyText, localCtx)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(
        subs: List<Triple<Subscription, Date, Long>>,
        isLoading: Boolean,
        emptyText: String,
        context: Context
    ) {
        val BackgroundPurple = Color(0xFF7A40F2)
        val DarkPurpleTrack = Color(0xFF4A2092)
        val LightPurpleProgress = Color(0xFFD0C8FF)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(BackgroundPurple)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: logo + refresh button
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = GlanceModifier.size(24.dp))
                    Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                        Image(
                            provider = ImageProvider(R.drawable.oroi_logo_white),
                            contentDescription = "Oroi",
                            modifier = GlanceModifier.size(64.dp)
                        )
                    }
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<RefreshAction>())
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                if (subs.isEmpty()) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = emptyText,
                            style = TextStyle(color = ColorProvider(Color.White))
                        )
                    }
                } else {
                    subs.forEach { (sub, _, daysLeft) ->
                        SubscriptionRow(
                            sub = sub,
                            daysLeft = daysLeft,
                            trackColor = DarkPurpleTrack,
                            progressColor = LightPurpleProgress,
                            context = context
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun SubscriptionRow(
        sub: Subscription,
        daysLeft: Long,
        trackColor: Color,
        progressColor: Color,
        context: Context
    ) {
        val cycleDays = when (sub.billingCycle) {
            BillingCycle.WEEKLY -> 7f
            BillingCycle.MONTHLY -> 30f
            BillingCycle.ANNUAL -> 365f
        }
        // Progress fills as renewal approaches: 0 = just renewed, 1 = renews today
        val progress = ((cycleDays - daysLeft) / cycleDays).coerceIn(0f, 1f)

        val daysText = context.getString(R.string.widget_days_left, daysLeft)
        val cycleTag = when (sub.billingCycle) {
            BillingCycle.WEEKLY -> context.getString(R.string.billing_badge_weekly)
            BillingCycle.MONTHLY -> context.getString(R.string.billing_badge_monthly)
            BillingCycle.ANNUAL -> context.getString(R.string.billing_badge_annual)
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Name
            Text(
                text = sub.name,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                modifier = GlanceModifier.width(80.dp),
                maxLines = 1
            )

            // Progress bar with amount overlay
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(8.dp),
                    color = ColorProvider(progressColor),
                    backgroundColor = ColorProvider(trackColor)
                )
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${sub.amount.toInt()}€ $cycleTag",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Days left
            Text(
                text = daysText,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier.width(40.dp)
            )
        }
    }

    private fun getLocalizedContext(context: Context): Context {
        val tag = context.getSharedPreferences(UserPreferencesRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(UserPreferencesRepository.LANGUAGE_TAG_KEY, "") ?: ""
        if (tag.isEmpty()) return context
        val locale = Locale.forLanguageTag(tag)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun calculateNextPayment(subscription: Subscription): Date {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        calendar.time = subscription.firstPaymentDate
        if (calendar.time.after(today.time)) return calendar.time
        while (calendar.time.before(today.time)) {
            when (subscription.billingCycle) {
                BillingCycle.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.ANNUAL -> calendar.add(Calendar.YEAR, 1)
            }
        }
        return calendar.time
    }

    private fun calculateDaysLeft(nextDate: Date): Long {
        val diff = nextDate.time - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs -> prefs[IsLoadingKey] = true }
        OroiWidget().update(context, glanceId)
        delay(1500)
        updateAppWidgetState(context, glanceId) { prefs -> prefs[IsLoadingKey] = false }
        OroiWidget().update(context, glanceId)
    }
}
