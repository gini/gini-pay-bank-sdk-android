package net.gini.pay.bank.capture.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import org.slf4j.LoggerFactory

internal class SimpleBusEventStore(context: Context) {
    private val mSharedPreferences: SharedPreferences =
        context.getSharedPreferences(APP_SIMPLE_EVENTS, Context.MODE_PRIVATE)

    private val appSimpleEventListeners = mutableSetOf<EventChangeListener>()

    private val sharedChangeListener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            for (listeners in appSimpleEventListeners) {
                if (key == listeners.key.name) {
                    sharedPreferences?.getBoolean(listeners.key.name, false)?.also {
                        listeners.valueChanged(it)
                    }

                }
            }
        }
    }

    fun containsEvent(event: BusEvent): Boolean = mSharedPreferences.contains(event.name)

    fun eventValue(event: BusEvent): Boolean =
        mSharedPreferences.getBoolean(event.name, false)

    fun saveEvent(event: BusEvent, value: Boolean = true) {
        mSharedPreferences.edit {
            putBoolean(event.name, value)
        }
        LOG.debug("Saved event {}", event.name)
    }

    fun registerChangeListener(listener: EventChangeListener) {
        if (appSimpleEventListeners.isEmpty()) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(sharedChangeListener)
        }

        appSimpleEventListeners.add(listener)
    }

    fun unregisterChangeListener(listener: EventChangeListener) {
        appSimpleEventListeners.remove(listener)
        if (appSimpleEventListeners.isEmpty()) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedChangeListener)
        }
    }

    @VisibleForTesting
    fun clearEvent(event: BusEvent) {
        mSharedPreferences.edit {
            remove(event.name)
        }
        LOG.debug("Cleared event {}", event.name)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SimpleBusEventStore::class.java)
        private const val APP_SIMPLE_EVENTS = "GOB_SIMPLE_EVENTS"
    }

    interface EventChangeListener {
        val key: BusEvent
        fun valueChanged(value: Boolean)
    }
}

internal enum class BusEvent {
    DISMISS_ONBOARDING_FRAGMENT
}
