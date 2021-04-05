package net.gini.pay.bank.capture.digitalinvoice.onboarding

/**
 * Created by Alpar Szotyori on 15.10.2020.
 *
 * Copyright (c) 2020 Gini GmbH.
 */

/**
 * Internal use only.
 *
 * @suppress
 */
interface DigitalInvoiceOnboardingFragmentListener {

    fun onCloseOnboarding(doNotShowAnymore: Boolean = false)
}