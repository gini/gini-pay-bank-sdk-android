package net.gini.pay.bank.capture.digitalinvoice.onboarding

import android.app.Activity
import net.gini.pay.bank.capture.util.SimpleBusEventStore
import net.gini.pay.bank.capture.util.OncePerInstallEvent
import net.gini.pay.bank.capture.util.OncePerInstallEventStore
import net.gini.pay.bank.capture.util.BusEvent

internal class DigitalOnboardingScreenPresenter(
    activity: Activity,
    view: DigitalOnboardingScreenContract.View,
    private val oncePerInstallEventStore: OncePerInstallEventStore = OncePerInstallEventStore(
        activity
    ),
    private val simpleBusEventStore: SimpleBusEventStore = SimpleBusEventStore(activity)
) : DigitalOnboardingScreenContract.Presenter(activity, view) {

    override var listener: DigitalInvoiceOnboardingFragmentListener? = null

    init {
        view.setPresenter(this)
    }

    override fun dismisOnboarding(doNotShowAnymore: Boolean) {
        if (doNotShowAnymore) {
            oncePerInstallEventStore.saveEvent(OncePerInstallEvent.SHOW_DIGITAL_INVOICE_ONBOARDING)
        }
        simpleBusEventStore.saveEvent(BusEvent.DISMISS_ONBOARDING_FRAGMENT)
        listener?.onCloseOnboarding()
    }

    override fun start() {
    }

    override fun stop() {
    }
}