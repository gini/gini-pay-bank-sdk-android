package net.gini.pay.bank.capture.digitalinvoice

import android.app.Activity
import androidx.annotation.VisibleForTesting
import net.gini.android.capture.GiniCapture
import net.gini.android.capture.network.model.GiniCaptureCompoundExtraction
import net.gini.android.capture.network.model.GiniCaptureReturnReason
import net.gini.android.capture.network.model.GiniCaptureSpecificExtraction
import net.gini.pay.bank.capture.util.OncePerInstallEvent
import net.gini.pay.bank.capture.util.OncePerInstallEventStore

/**
 * Created by Alpar Szotyori on 05.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

internal open class DigitalInvoiceScreenPresenter(
    activity: Activity,
    view: DigitalInvoiceScreenContract.View,
    val extractions: Map<String, GiniCaptureSpecificExtraction> = emptyMap(),
    val compoundExtractions: Map<String, GiniCaptureCompoundExtraction> = emptyMap(),
    val returnReasons: List<GiniCaptureReturnReason> = emptyList(),
    private val isInaccurateExtraction: Boolean = false,
    private var onboardingDisplayed: Boolean = false,
    private val oncePerInstallEventStore: OncePerInstallEventStore = OncePerInstallEventStore(activity)
) :
    DigitalInvoiceScreenContract.Presenter(activity, view) {

    override var listener: DigitalInvoiceFragmentListener? = null

    private var footerDetails = DigitalInvoiceScreenContract.FooterDetails(inaccurateExtraction = isInaccurateExtraction)

    @VisibleForTesting
    val digitalInvoice: DigitalInvoice

    init {
        view.setPresenter(this)
        digitalInvoice = DigitalInvoice(extractions, compoundExtractions)
    }

    override fun selectLineItem(lineItem: SelectableLineItem) {
        digitalInvoice.selectLineItem(lineItem)
        updateView()
    }

    override fun deselectLineItem(lineItem: SelectableLineItem) {
        if (returnReasons.isEmpty()) {
            digitalInvoice.deselectLineItem(lineItem, null)
            updateView()
        } else {
            view.showReturnReasonDialog(returnReasons) { selectedReason ->
                if (selectedReason != null) {
                    digitalInvoice.deselectLineItem(lineItem, selectedReason)
                } else {
                    digitalInvoice.selectLineItem(lineItem)
                }
                updateView()
            }
        }
    }

    override fun editLineItem(lineItem: SelectableLineItem) {
        listener?.onEditLineItem(lineItem)
    }

    override fun userFeedbackReceived(helpful: Boolean) {
        // TODO
    }

    override fun pay() {
        digitalInvoice.updateLineItemExtractionsWithReviewedLineItems()
        digitalInvoice.updateAmountToPayExtractionWithTotalPrice()
        if (GiniCapture.hasInstance()) {
            GiniCapture.getInstance().giniCaptureNetworkApi?.setUpdatedCompoundExtractions(
                digitalInvoice.compoundExtractions
            )
        }
        listener?.onPayInvoice(digitalInvoice.extractions, digitalInvoice.compoundExtractions)
    }

    override fun skip() {
        listener?.onPayInvoice(emptyMap(), emptyMap())
    }

    override fun updateLineItem(selectableLineItem: SelectableLineItem) {
        digitalInvoice.updateLineItem(selectableLineItem)
        updateView()
    }

    override fun start() {
        updateView()
        if (!onboardingDisplayed && !oncePerInstallEventStore.containsEvent(OncePerInstallEvent.SHOW_DIGITAL_INVOICE_ONBOARDING)) {
            listener?.showOnboarding()
        }
    }

    override fun stop() {
    }

    @VisibleForTesting
    internal fun updateView() {
        view.apply {
            showLineItems(digitalInvoice.selectableLineItems, isInaccurateExtraction)
            showAddons(digitalInvoice.addons)
            digitalInvoice.selectedAndTotalLineItemsCount().let { (selected, total) ->
                footerDetails = footerDetails
                    .copy(
                        totalGrossPriceIntegralAndFractionalParts = digitalInvoice.totalPriceIntegralAndFractionalParts(),
                        buttonEnabled = selected > 0,
                        count = selected,
                        total = total
                    )
                updateFooterDetails(footerDetails)
            }
        }
    }
}