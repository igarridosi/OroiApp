package com.example.oroiapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.oroiapp.MainActivity
import com.example.oroiapp.R
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

val IsLoadingKey = booleanPreferencesKey("is_loading")

// Widget header height + padding (dp) consumed before items
private const val HEADER_HEIGHT_DP = 52f
// Height each subscription row occupies (text + bar + spacing)
private const val ITEM_HEIGHT_DP  = 42f

class OroiWidget : GlanceAppWidget() {

    // Exact mode: LocalSize.current reflects the actual widget dimensions
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = OroiViewModelFactory.dao
        val allSubs = try {
            dao.getAllSubscriptions().first()
        } catch (e: Exception) {
            emptyList()
        }

        // All billing cycles, sorted by urgency (fewest days first), take max 8
        val sortedSubs = allSubs
            .map { sub ->
                val nextDate = calculateNextPayment(sub)
                Triple(sub, nextDate, calculateDaysLeft(nextDate))
            }
            .sortedBy { it.third }
            .take(8)

        val localCtx = getLocalizedContext(context)
        val emptyText = localCtx.getString(R.string.widget_empty_text)

        provideContent {
            GlanceTheme {
                WidgetContent(sortedSubs, emptyText, localCtx)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(
        subs: List<Triple<Subscription, Date, Long>>,
        emptyText: String,
        context: Context
    ) {
        val Purple    = Color(0xFF7A40F2)
        val Track     = Color(0xFF4A2092)
        val Divider   = Color(0x33FFFFFF)

        // How many rows fit in the current widget height
        val widgetHeight = LocalSize.current.height.value
        val displayCount = ((widgetHeight - HEADER_HEIGHT_DP) / ITEM_HEIGHT_DP)
            .toInt()
            .coerceIn(1, subs.size.coerceAtLeast(1))
        val visible = subs.take(displayCount)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Purple)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {

                // ── Compact header ───────────────────────────────────────
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.oroi_logo_white),
                        contentDescription = "Oroi",
                        modifier = GlanceModifier.size(22.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "oroi",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier
                            .size(18.dp)
                            .clickable(actionRunCallback<RefreshAction>())
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))
                Spacer(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Divider)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))

                // ── Subscription rows ─────────────────────────────────────
                if (visible.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyText,
                            style = TextStyle(
                                color = ColorProvider(Color(0xAAFFFFFF)),
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    visible.forEachIndexed { index, (sub, _, daysLeft) ->
                        SubscriptionRow(sub = sub, daysLeft = daysLeft, trackColor = Track, context = context)
                        if (index < visible.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun SubscriptionRow(
        sub: Subscription,
        daysLeft: Long,
        trackColor: Color,
        context: Context
    ) {
        // ── Progress: normalized to 30-day urgency window ─────────────────
        // All billing cycles are comparable: bar fills as renewal approaches.
        // daysLeft ≥ 30 → empty bar (no urgency); daysLeft = 0 → full bar.
        val progress = (1f - daysLeft / 30f).coerceIn(0f, 1f)

        // ── Urgency colors ────────────────────────────────────────────────
        val (daysColor, barColor) = when {
            daysLeft <= 3  -> Color(0xFFFF6B6B) to Color(0xFFFF6B6B) // red
            daysLeft <= 7  -> Color(0xFFFFBF69) to Color(0xFFFFBF69) // amber
            else           -> Color(0xAAFFFFFF) to Color(0xFFD0C8FF) // muted/purple
        }

        val cycleTag = when (sub.billingCycle) {
            BillingCycle.WEEKLY  -> context.getString(R.string.billing_badge_weekly)
            BillingCycle.MONTHLY -> context.getString(R.string.billing_badge_monthly)
            BillingCycle.ANNUAL  -> context.getString(R.string.billing_badge_annual)
        }
        val amountText = "%s€/%s".format(
            if (sub.amount == sub.amount.toLong().toDouble()) sub.amount.toLong().toString()
            else String.format(Locale.US, "%.2f", sub.amount),
            cycleTag
        )
        val daysText = context.getString(R.string.widget_days_left, daysLeft)

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            // Line 1: Name | Amount+cycle | Days
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sub.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1
                )
                Text(
                    text = amountText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xCCFFFFFF)),
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                Text(
                    text = daysText,
                    style = TextStyle(
                        color = ColorProvider(daysColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Line 2: Thin urgency progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .cornerRadius(4.dp),
                color = ColorProvider(barColor),
                backgroundColor = ColorProvider(trackColor)
            )
        }
    }

    // Apply saved in-app language preference to the widget's string context
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
                BillingCycle.WEEKLY  -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.ANNUAL  -> calendar.add(Calendar.YEAR, 1)
            }
        }
        return calendar.time
    }

    private fun calculateDaysLeft(nextDate: Date): Long =
        TimeUnit.MILLISECONDS.toDays(nextDate.time - System.currentTimeMillis()).coerceAtLeast(0)
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
