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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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

// Space consumed by header + padding before any item starts (dp)
private const val HEADER_DP  = 62f
// Minimum height each item needs (without inter-item spacing)
private const val ITEM_DP    = 36f

class OroiWidget : GlanceAppWidget() {

    // Exact size → LocalSize.current reflects the real widget dimensions
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val allSubs = try {
            OroiViewModelFactory.dao.getAllSubscriptions().first()
        } catch (e: Exception) {
            emptyList()
        }

        val sortedSubs = allSubs
            .map { sub ->
                val next = calculateNextPayment(sub)
                Triple(sub, next, calculateDaysLeft(next))
            }
            .sortedBy { it.third }
            .take(8)

        val ctx = localizedContext(context)

        provideContent {
            GlanceTheme {
                WidgetContent(sortedSubs, ctx.getString(R.string.widget_empty_text), ctx)
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
        val Purple  = Color(0xFF7A40F2)
        val Track   = Color(0xFF4A2092)
        val Divider = Color(0x33FFFFFF)

        // How many items fit; remaining space is distributed via defaultWeight spacers
        val height = LocalSize.current.height.value
        val maxCount = ((height - HEADER_DP) / ITEM_DP).toInt().coerceIn(1, 8)
        val visible  = subs.take(maxCount)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Purple)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {

                // ── Header ───────────────────────────────────────────────────
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.oroi_logo_white),
                        contentDescription = "Oroi",
                        modifier = GlanceModifier.size(58.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "oroi",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))
                Spacer(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Divider)
                )

                // ── Items distributed evenly (SpaceEvenly via defaultWeight) ──
                if (visible.isEmpty()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = emptyText,
                            style = TextStyle(color = ColorProvider(Color(0xAAFFFFFF)), fontSize = 12.sp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                } else {
                    // SpaceEvenly: equal gap before first, between each, after last
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    visible.forEachIndexed { index, (sub, _, daysLeft) ->
                        SubscriptionRow(sub = sub, daysLeft = daysLeft, trackColor = Track, context = context)
                        Spacer(modifier = GlanceModifier.defaultWeight())
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
        // Urgency: normalize to 30-day window — all cycles comparable
        val progress = (1f - daysLeft / 30f).coerceIn(0f, 1f)

        val (daysColor, barColor) = when {
            daysLeft <= 3  -> Color(0xFFFF6B6B) to Color(0xFFFF6B6B) // red
            daysLeft <= 7  -> Color(0xFFFFBF69) to Color(0xFFFFBF69) // amber
            else           -> Color(0xFFFFFFFF) to Color(0xFFD0C8FF) // white / soft purple
        }

        val cycleTag = when (sub.billingCycle) {
            BillingCycle.WEEKLY  -> context.getString(R.string.billing_badge_weekly)
            BillingCycle.MONTHLY -> context.getString(R.string.billing_badge_monthly)
            BillingCycle.ANNUAL  -> context.getString(R.string.billing_badge_annual)
        }
        val rawAmount = if (sub.amount == sub.amount.toLong().toDouble())
            sub.amount.toLong().toString()
        else
            String.format(Locale.US, "%.2f", sub.amount)
        val amountText = "$rawAmount€/$cycleTag"
        val daysText   = context.getString(R.string.widget_days_left, daysLeft)

        Column(modifier = GlanceModifier.fillMaxWidth()) {

            // Line 1 — Name (primary) + Days (urgent, bold number)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sub.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1
                )
                Text(
                    text = daysText,
                    style = TextStyle(
                        color = ColorProvider(daysColor),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Line 2 — Progress bar + Amount (secondary)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(5.dp)
                        .cornerRadius(5.dp),
                    color = ColorProvider(barColor),
                    backgroundColor = ColorProvider(trackColor)
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                Text(
                    text = amountText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xCCFFFFFF)),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    private fun localizedContext(context: Context): Context {
        val tag = context.getSharedPreferences(UserPreferencesRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(UserPreferencesRepository.LANGUAGE_TAG_KEY, "") ?: ""
        if (tag.isEmpty()) return context
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(tag))
        return context.createConfigurationContext(config)
    }

    private fun calculateNextPayment(sub: Subscription): Date {
        val cal   = Calendar.getInstance().also { it.time = sub.firstPaymentDate }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (cal.time.after(today.time)) return cal.time
        while (cal.time.before(today.time)) {
            when (sub.billingCycle) {
                BillingCycle.WEEKLY  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> cal.add(Calendar.MONTH, 1)
                BillingCycle.ANNUAL  -> cal.add(Calendar.YEAR, 1)
            }
        }
        return cal.time
    }

    private fun calculateDaysLeft(next: Date): Long =
        TimeUnit.MILLISECONDS.toDays(next.time - System.currentTimeMillis()).coerceAtLeast(0)
}
