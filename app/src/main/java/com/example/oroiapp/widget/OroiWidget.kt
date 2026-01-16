package com.example.oroiapp.widget

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
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
        // EGOERA LORTU
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val isLoading = prefs[IsLoadingKey] ?: false // Lehenetsita false

        // 1. DATUAK LORTU
        val dao = OroiViewModelFactory.dao

        val allSubs = try {
            dao.getAllSubscriptions().first()
        } catch (e: Exception) {
            emptyList()
        }

        // 2. FILTRATU ETA ORDENATU
        // Bakarrik hilerokoak, datak kalkulatu, ordenatu eta lehenengo 4ak hartu
        val targetSubs = allSubs
            .filter { it.billingCycle == BillingCycle.MONTHLY }
            .map { sub ->
                val nextDate = calculateNextPayment(sub)
                val daysLeft = calculateDaysLeft(nextDate)
                Triple(sub, nextDate, daysLeft)
            }
            .sortedBy { it.third } // Egun gutxien falta zaiena lehenengo
            .take(4)

        provideContent {
            GlanceTheme {
                // Pasatu 'isLoading' WidgetContent-era
                WidgetContent(targetSubs, isLoading)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(subs: List<Triple<Subscription, Date, Long>>, isLoading: Boolean) {
        // Diseinuaren Koloreak
        val BackgroundPurple = Color(0xFF7A40F2)
        val DarkPurpleTrack = Color(0xFF4A2092)
        val LightPurpleProgress = Color(0xFFD0C8FF)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(BackgroundPurple)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()) // Widget osoak app-a irekitzen du
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- GOIBURUA (Logo + Refresh) ---
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ezkerrean espazio hutsa orekatzeko
                    Spacer(modifier = GlanceModifier.size(24.dp))

                    // Logo erdian
                    Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                        Image(
                            provider = ImageProvider(R.drawable.oroi_logo_white),
                            contentDescription = "Oroi",
                            modifier = GlanceModifier.size(64.dp)
                        )
                    }

                    if (isLoading) {
                        // Kargatzen ari bada, gurpila erakutsi
                        CircularProgressIndicator(
                            modifier = GlanceModifier.size(24.dp),
                            color = ColorProvider(Color.White)
                        )
                    } else {
                        // Bestela, botoia erakutsi
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_popup_sync),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<RefreshAction>())
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                if (subs.isEmpty()) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ez dago hileko harpidetzarik",
                            style = TextStyle(color = ColorProvider(Color.White))
                        )
                    }
                } else {
                    // ZERRENDA
                    subs.forEach { (sub, _, daysLeft) ->
                        SubscriptionRow(
                            sub = sub,
                            daysLeft = daysLeft,
                            trackColor = DarkPurpleTrack,
                            progressColor = LightPurpleProgress
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
        progressColor: Color
    ) {
        // Progresoaren kalkulua (30 eguneko zikloa)
        val progress = ((30 - daysLeft).toFloat() / 30f).coerceIn(0f, 1f)
        val annualCost = sub.amount * 12

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. IZENA
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

            // 2. PROGRESO BARRA + PREZIOA
            // Box bat erabiltzen dugu barra eta testua gainjartzeko
            Box(
                modifier = GlanceModifier
                    .defaultWeight() // Hartu libre dagoen espazio guztia
                    .height(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // A) PROGRESO BARRA OFIZIALA
                // Glance-k badu osagai hau, eta cornerRadius onartzen du
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(8.dp), // <-- ERTZ BIRIBILAK HEMEN APLIKATU
                    color = ColorProvider(progressColor),
                    backgroundColor = ColorProvider(trackColor)
                )

                // B) TESTUA (Prezioa) - Barraren gainean (Eskuinean)
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${annualCost.toInt()}€",
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

            // 3. EGUNAK
            Text(
                text = "$daysLeft Egun",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier.width(50.dp)
            )
        }
    }

    // --- LOGIKA LAGUNTZAILEA ---
    private fun calculateNextPayment(subscription: Subscription): Date {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0)

        calendar.time = subscription.firstPaymentDate

        if (calendar.time.after(today.time)) return calendar.time

        while (calendar.time.before(today.time)) {
            calendar.add(Calendar.MONTH, 1)
        }
        return calendar.time
    }

    private fun calculateDaysLeft(nextDate: Date): Long {
        val now = System.currentTimeMillis()
        val diff = nextDate.time - now
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // 1. Egoera 'KARGATZEN' jarri
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[IsLoadingKey] = true
        }
        // Behartu Widget-a marraztera (Loading gurpila agertzeko)
        OroiWidget().update(context, glanceId)

        // 2. Itxaron pixka bat (segundu bat eta erdi) animazioa ikusteko
        delay(1500)

        // 3. Egoera 'AMAITUTA' jarri
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[IsLoadingKey] = false
        }
        // Behartu Widget-a marraztera (Botoia berriro agertzeko)
        OroiWidget().update(context, glanceId)
    }
}