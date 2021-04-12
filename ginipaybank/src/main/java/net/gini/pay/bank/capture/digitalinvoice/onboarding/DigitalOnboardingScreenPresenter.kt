package net.gini.pay.bank.capture.digitalinvoice.onboarding

import android.app.Activity
import net.gini.pay.bank.capture.util.OncePerInstallEvent
import net.gini.pay.bank.capture.util.OncePerInstallEventStore

internal class DigitalOnboardingScreenPresenter(
    activity: Activity,
    view: DigitalOnboardingScreenContract.View,
    val oncePerInstallEventStore: OncePerInstallEventStore = OncePerInstallEventStore(activity)
): DigitalOnboardingScreenContract.Presenter(activity, view) {

    override var listener: DigitalInvoiceOnboardingFragmentListener? = null
    init {
        view.setPresenter(this)
    }

    override fun dismisOnboarding(doNotShowAnymore: Boolean) {
        if (doNotShowAnymore) {
            oncePerInstallEventStore.saveEvent(OncePerInstallEvent.SHOW_DIGITAL_INVOICE_ONBOARDING)
        }
        listener?.onCloseOnboarding()
    }

    override fun start() {
    }

    override fun stop() {
    }
}